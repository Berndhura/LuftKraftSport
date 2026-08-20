const express = require('express');
const { db } = require('../db');
const { requireAuth } = require('../auth');
const { toRowItem, paginate } = require('./articles');

const router = express.Router();

router.get('/ids', requireAuth, (req, res) => {
    const rows = db.prepare('SELECT article_id FROM bookmarks WHERE user_id = ?').all(req.user.id);
    res.json(rows.map(r => r.article_id));
});

router.get('/', requireAuth, (req, res) => {
    const page = Math.max(0, parseInt(req.query.page ?? '0', 10));
    const size = Math.min(100, Math.max(1, parseInt(req.query.size ?? '10', 10)));
    const lat = req.query.lat ? parseFloat(req.query.lat) : null;
    const lng = req.query.lng ? parseFloat(req.query.lng) : null;

    const rows = db.prepare(`
        SELECT a.* FROM articles a
        JOIN bookmarks b ON b.article_id = a.id
        WHERE b.user_id = ?
        ORDER BY a.date DESC
    `).all(req.user.id);

    res.json(paginate(rows, page, size, lat, lng));
});

router.delete('/:articleId', requireAuth, (req, res) => {
    db.prepare('DELETE FROM bookmarks WHERE user_id = ? AND article_id = ?').run(req.user.id, req.params.articleId);
    res.json('ok');
});

module.exports = router;
