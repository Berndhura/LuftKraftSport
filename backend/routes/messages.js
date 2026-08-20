const express = require('express');
const { db } = require('../db');
const { requireAuth } = require('../auth');

const router = express.Router();

router.get('/forUser', requireAuth, (req, res) => {
    const rows = db.prepare(`
        SELECT m.*, u.name AS other_name
        FROM messages m
        JOIN users u ON u.id = CASE WHEN m.from_user = ? THEN m.to_user ELSE m.from_user END
        WHERE m.from_user = ? OR m.to_user = ?
        ORDER BY m.date DESC
    `).all(req.user.id, req.user.id, req.user.id);

    const seen = new Set();
    const grouped = [];
    for (const m of rows) {
        const key = `${m.article_id}_${m.from_user === req.user.id ? m.to_user : m.from_user}`;
        if (seen.has(key)) continue;
        seen.add(key);
        const chatPartner = m.from_user === req.user.id ? m.to_user : m.from_user;
        grouped.push({
            message: m.message,
            name: m.other_name,
            url: '',
            date: m.date,
            idFrom: m.from_user,
            idTo: m.to_user,
            articleId: m.article_id,
            chatPartner,
        });
    }
    res.json(grouped);
});

router.get('/forArticle', requireAuth, (req, res) => {
    const chatPartner = req.query.sender;
    const articleId = parseInt(req.query.articleId, 10);
    const rows = db.prepare(`
        SELECT * FROM messages
        WHERE article_id = ?
          AND ((from_user = ? AND to_user = ?) OR (from_user = ? AND to_user = ?))
        ORDER BY date ASC
    `).all(articleId, req.user.id, chatPartner, chatPartner, req.user.id);

    res.json(rows.map(m => ({
        message: m.message,
        date: m.date,
        idFrom: m.from_user,
        chatPartner: m.from_user === req.user.id ? m.to_user : m.from_user,
    })));
});

router.post('/', requireAuth, (req, res) => {
    const { message, articleId, idTo } = req.query;
    if (!message || !articleId || !idTo) {
        return res.status(400).json({ error: 'missing params' });
    }
    db.prepare(`
        INSERT INTO messages (from_user, to_user, article_id, message, date)
        VALUES (?, ?, ?, ?, ?)
    `).run(req.user.id, idTo, parseInt(articleId, 10), message, Date.now());
    res.json('ok');
});

module.exports = router;
