const express = require('express');
const path = require('path');
const fs = require('fs');
const multer = require('multer');
const { db, UPLOADS_DIR } = require('../db');
const { requireAuth, optionalAuth } = require('../auth');

const router = express.Router();

function toRad(deg) { return deg * Math.PI / 180; }
function haversineKm(lat1, lng1, lat2, lng2) {
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLng = toRad(lng2 - lng1);
    const a = Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
    return 2 * R * Math.asin(Math.sqrt(a));
}

function pictureIdsFor(articleId) {
    return db.prepare('SELECT id FROM pictures WHERE article_id = ? ORDER BY id').all(articleId)
        .map(r => String(r.id));
}

function bookmarkCountFor(articleId) {
    return db.prepare('SELECT COUNT(*) AS c FROM bookmarks WHERE article_id = ?').get(articleId).c;
}

function toRowItem(row, viewerLat, viewerLng) {
    const pictureIds = pictureIdsFor(row.id);
    const bookmarks = bookmarkCountFor(row.id);
    const item = {
        id: row.id,
        title: row.title,
        urls: pictureIds.join(','),
        description: row.description,
        phone: row.phone,
        date: row.date,
        price: row.price,
        location: { type: 'Point', coordinates: [row.lng, row.lat] },
        userId: row.user_id,
        views: String(row.views),
        bookmarks: String(bookmarks),
        pictureIds,
        locationName: row.location_name,
    };
    if (typeof viewerLat === 'number' && typeof viewerLng === 'number' &&
        !(viewerLat === 0 && viewerLng === 0)) {
        item.distance = Math.round(haversineKm(viewerLat, viewerLng, row.lat, row.lng));
    }
    return item;
}

function paginate(rows, page, size, viewerLat, viewerLng) {
    const total = rows.length;
    const pages = Math.max(1, Math.ceil(total / size));
    const start = page * size;
    const slice = rows.slice(start, start + size);
    return {
        ads: slice.map(r => toRowItem(r, viewerLat, viewerLng)),
        page, size, pages, total,
    };
}

router.get('/', optionalAuth, (req, res) => {
    const page = Math.max(0, parseInt(req.query.page ?? '0', 10));
    const size = Math.min(100, Math.max(1, parseInt(req.query.size ?? '10', 10)));
    const priceFrom = req.query.priceFrom ? parseInt(req.query.priceFrom, 10) : null;
    const priceTo = req.query.priceTo ? parseInt(req.query.priceTo, 10) : null;
    const distance = req.query.distance ? parseInt(req.query.distance, 10) : null;
    const description = req.query.description || null;
    const userIdFilter = req.query.userId || null;
    const lat = req.query.lat ? parseFloat(req.query.lat) : null;
    const lng = req.query.lng ? parseFloat(req.query.lng) : null;

    let rows = db.prepare('SELECT * FROM articles ORDER BY date DESC').all();

    if (userIdFilter) rows = rows.filter(r => r.user_id === userIdFilter);
    if (priceFrom !== null) rows = rows.filter(r => r.price >= priceFrom);
    if (priceTo !== null) rows = rows.filter(r => r.price <= priceTo);
    if (description) {
        const q = description.toLowerCase();
        rows = rows.filter(r =>
            (r.title || '').toLowerCase().includes(q) ||
            (r.description || '').toLowerCase().includes(q));
    }
    if (distance && lat !== null && lng !== null && !(lat === 0 && lng === 0)) {
        rows = rows.filter(r => haversineKm(lat, lng, r.lat, r.lng) <= distance);
    }

    res.json(paginate(rows, page, size, lat, lng));
});

router.get('/my', requireAuth, (req, res) => {
    const page = Math.max(0, parseInt(req.query.page ?? '0', 10));
    const size = Math.min(100, Math.max(1, parseInt(req.query.size ?? '10', 10)));
    const rows = db.prepare('SELECT * FROM articles WHERE user_id = ? ORDER BY date DESC').all(req.user.id);
    res.json(paginate(rows, page, size));
});

router.get('/:id', (req, res) => {
    const row = db.prepare('SELECT * FROM articles WHERE id = ?').get(req.params.id);
    if (!row) return res.status(404).json({ error: 'article not found' });
    res.json({
        id: row.id,
        title: row.title,
        description: row.description,
        urls: pictureIdsFor(row.id).join(','),
        userId: row.user_id,
        price: row.price,
        date: row.date,
        views: row.views,
        bookmarks: bookmarkCountFor(row.id),
        location: { type: 'Point', coordinates: [row.lng, row.lat] },
        locationName: row.location_name,
    });
});

router.post('/', requireAuth, express.json(), (req, res) => {
    const b = req.body || {};
    const lat = b.location?.coordinates?.[1] ?? 0;
    const lng = b.location?.coordinates?.[0] ?? 0;
    const now = Date.now();
    const result = db.prepare(`
        INSERT INTO articles (user_id, title, description, phone, price, lat, lng, location_name, date, views, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)
    `).run(req.user.id, b.title || '', b.description || '', b.phone || '', b.price || 0,
        lat, lng, b.locationName || '', now, now);
    const row = db.prepare('SELECT * FROM articles WHERE id = ?').get(result.lastInsertRowid);
    res.json(toRowItem(row));
});

router.delete('/:id', requireAuth, (req, res) => {
    const row = db.prepare('SELECT user_id FROM articles WHERE id = ?').get(req.params.id);
    if (!row) return res.status(404).json({ error: 'not found' });
    if (row.user_id !== req.user.id) return res.status(403).json({ error: 'forbidden' });

    const pics = db.prepare('SELECT filename FROM pictures WHERE article_id = ?').all(req.params.id);
    for (const p of pics) {
        try { fs.unlinkSync(path.join(UPLOADS_DIR, p.filename)); } catch (_) {}
    }
    db.prepare('DELETE FROM articles WHERE id = ?').run(req.params.id);
    res.json('ok');
});

router.post('/:id/increaseViewCount', (req, res) => {
    db.prepare('UPDATE articles SET views = views + 1 WHERE id = ?').run(req.params.id);
    res.json('ok');
});

router.post('/:id/bookmark', requireAuth, (req, res) => {
    db.prepare('INSERT OR IGNORE INTO bookmarks (user_id, article_id) VALUES (?, ?)').run(req.user.id, req.params.id);
    res.json('ok');
});

const upload = multer({
    storage: multer.diskStorage({
        destination: UPLOADS_DIR,
        filename: (req, file, cb) => {
            const ext = path.extname(file.originalname) || '.jpg';
            const name = `article_${req.params.id}_${Date.now()}${ext}`;
            cb(null, name);
        },
    }),
});

router.post('/:id/addPicture', requireAuth, upload.single('file'), (req, res) => {
    if (!req.file) return res.status(400).json({ error: 'no file' });
    db.prepare('INSERT INTO pictures (article_id, filename) VALUES (?, ?)').run(req.params.id, req.file.filename);
    res.json('ok');
});

router.delete('/:articleId/:pictureId/deletePicture', requireAuth, (req, res) => {
    const pic = db.prepare('SELECT filename FROM pictures WHERE id = ? AND article_id = ?')
        .get(req.params.pictureId, req.params.articleId);
    if (pic) {
        try { fs.unlinkSync(path.join(UPLOADS_DIR, pic.filename)); } catch (_) {}
        db.prepare('DELETE FROM pictures WHERE id = ?').run(req.params.pictureId);
    }
    res.json('ok');
});

module.exports = { router, toRowItem, paginate };
