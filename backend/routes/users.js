const express = require('express');
const crypto = require('crypto');
const bcrypt = require('bcryptjs');
const { db } = require('../db');
const { requireAuth } = require('../auth');

const router = express.Router();

function newToken() {
    return crypto.randomBytes(16).toString('hex');
}

function newUserId() {
    return 'u-' + crypto.randomBytes(8).toString('hex');
}

router.post('/register', (req, res) => {
    const { name, email, password } = req.query;
    if (!email || !password) return res.status(400).json({ error: 'email + password required' });

    const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(email);
    if (existing) return res.status(409).json({ error: 'email already registered' });

    const userId = newUserId();
    const activationCode = crypto.randomBytes(4).toString('hex');
    db.prepare(`
        INSERT INTO users (id, name, email, password_hash, activation_code, activated, profile_picture_url, created_at)
        VALUES (?, ?, ?, ?, ?, 1, '', ?)
    `).run(userId, name || email, email, bcrypt.hashSync(password, 10), activationCode, Date.now());

    console.log(`[users] registered ${email} (activation: ${activationCode})`);
    res.json('ok');
});

router.post('/activate', (req, res) => {
    const { email, activation_code } = req.query;
    const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
    if (!user) return res.status(404).json({ error: 'unknown email' });
    if (user.activation_code !== activation_code) {
        return res.status(400).json({ error: 'bad activation code' });
    }
    db.prepare('UPDATE users SET activated = 1 WHERE id = ?').run(user.id);
    res.json('ok');
});

router.post('/login', (req, res) => {
    const { email, password } = req.query;
    const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
    if (!user || !user.password_hash || !bcrypt.compareSync(password, user.password_hash)) {
        return res.status(401).json({ error: 'invalid credentials' });
    }
    const token = user.token || newToken();
    if (!user.token) {
        db.prepare('UPDATE users SET token = ? WHERE id = ?').run(token, user.id);
    }
    res.json({
        id: user.id,
        email: user.email,
        token,
        name: user.name,
        profilePictureUrl: user.profile_picture_url || '',
        numberOfArticles: db.prepare('SELECT COUNT(*) AS c FROM articles WHERE user_id = ?').get(user.id).c,
    });
});

router.post('/sendToken', requireAuth, (req, res) => {
    const { deviceToken } = req.query;
    db.prepare('UPDATE users SET device_token = ? WHERE id = ?').run(deviceToken || null, req.user.id);
    res.json('ok');
});

router.post('/profilePictureUrl', requireAuth, (req, res) => {
    const { url } = req.query;
    db.prepare('UPDATE users SET profile_picture_url = ? WHERE id = ?').run(url || '', req.user.id);
    res.json('ok');
});

router.get('/:id', (req, res) => {
    const user = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
    if (!user) return res.status(404).json({ error: 'not found' });
    res.json({
        id: user.id,
        email: user.email,
        name: user.name,
        profilePictureUrl: user.profile_picture_url || '',
        numberOfArticles: db.prepare('SELECT COUNT(*) AS c FROM articles WHERE user_id = ?').get(user.id).c,
    });
});

module.exports = router;
