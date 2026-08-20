const { db } = require('./db');

function currentUser(token) {
    if (!token) return null;
    return db.prepare('SELECT * FROM users WHERE token = ?').get(token) || null;
}

function requireAuth(req, res, next) {
    const user = currentUser(req.query.token);
    if (!user) {
        return res.status(401).json({ error: 'invalid or missing token' });
    }
    req.user = user;
    next();
}

function optionalAuth(req, res, next) {
    req.user = currentUser(req.query.token);
    next();
}

module.exports = { requireAuth, optionalAuth, currentUser };
