const express = require('express');
const { db } = require('../db');
const { requireAuth } = require('../auth');

const router = express.Router();

router.get('/', requireAuth, (req, res) => {
    const rows = db.prepare('SELECT * FROM searches WHERE user_id = ? ORDER BY created_at DESC').all(req.user.id);
    res.json(rows.map(r => ({
        id: r.id,
        description: r.description,
        priceFrom: r.price_from,
        priceTo: r.price_to,
        lat: r.lat,
        lng: r.lng,
        distance: r.distance,
    })));
});

router.post('/new', requireAuth, (req, res) => {
    const { description, priceFrom, priceTo, lat, lng, distance } = req.query;
    db.prepare(`
        INSERT INTO searches (user_id, description, price_from, price_to, lat, lng, distance, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `).run(req.user.id, description || '',
        priceFrom ? parseInt(priceFrom, 10) : null,
        priceTo ? parseInt(priceTo, 10) : null,
        lat ? parseFloat(lat) : null,
        lng ? parseFloat(lng) : null,
        distance ? parseInt(distance, 10) : null,
        Date.now());
    res.json('ok');
});

router.delete('/:id', requireAuth, (req, res) => {
    db.prepare('DELETE FROM searches WHERE id = ? AND user_id = ?').run(req.params.id, req.user.id);
    res.json('ok');
});

module.exports = router;
