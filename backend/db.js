const path = require('path');
const fs = require('fs');
const Database = require('better-sqlite3');
const bcrypt = require('bcryptjs');
const crypto = require('crypto');
const { stripedPng } = require('./pngGen');

const DB_PATH = path.join(__dirname, 'data', 'lks.db');
const UPLOADS_DIR = path.join(__dirname, 'data', 'uploads');

if (!fs.existsSync(path.dirname(DB_PATH))) {
    fs.mkdirSync(path.dirname(DB_PATH), { recursive: true });
}
if (!fs.existsSync(UPLOADS_DIR)) {
    fs.mkdirSync(UPLOADS_DIR, { recursive: true });
}

const db = new Database(DB_PATH);
db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

function init() {
    db.exec(`
        CREATE TABLE IF NOT EXISTS users (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL,
            password_hash TEXT,
            token TEXT UNIQUE,
            activation_code TEXT,
            activated INTEGER DEFAULT 1,
            profile_picture_url TEXT DEFAULT '',
            device_token TEXT,
            created_at INTEGER NOT NULL
        );

        CREATE TABLE IF NOT EXISTS articles (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT NOT NULL,
            title TEXT NOT NULL,
            description TEXT,
            phone TEXT,
            price REAL,
            lat REAL,
            lng REAL,
            location_name TEXT,
            date INTEGER NOT NULL,
            views INTEGER DEFAULT 0,
            created_at INTEGER NOT NULL,
            FOREIGN KEY(user_id) REFERENCES users(id)
        );

        CREATE TABLE IF NOT EXISTS pictures (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            article_id INTEGER NOT NULL,
            filename TEXT NOT NULL,
            FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS bookmarks (
            user_id TEXT NOT NULL,
            article_id INTEGER NOT NULL,
            PRIMARY KEY(user_id, article_id),
            FOREIGN KEY(user_id) REFERENCES users(id),
            FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            from_user TEXT NOT NULL,
            to_user TEXT NOT NULL,
            article_id INTEGER NOT NULL,
            message TEXT NOT NULL,
            date INTEGER NOT NULL
        );

        CREATE TABLE IF NOT EXISTS searches (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT NOT NULL,
            description TEXT,
            price_from INTEGER,
            price_to INTEGER,
            lat REAL,
            lng REAL,
            distance INTEGER,
            created_at INTEGER NOT NULL,
            FOREIGN KEY(user_id) REFERENCES users(id)
        );

        CREATE INDEX IF NOT EXISTS idx_articles_user ON articles(user_id);
        CREATE INDEX IF NOT EXISTS idx_pictures_article ON pictures(article_id);
        CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON bookmarks(user_id);
    `);
}

function seed() {
    const userCount = db.prepare('SELECT COUNT(*) AS c FROM users').get().c;
    if (userCount > 0) return;

    console.log('[seed] empty DB detected, seeding...');

    const now = Date.now();
    const testToken = crypto.randomBytes(16).toString('hex');
    const testPasswordHash = bcrypt.hashSync('test1234', 10);
    const testUserId = 'mock-user-1';
    const otherUserId = 'mock-user-2';

    db.prepare(`
        INSERT INTO users (id, name, email, password_hash, token, activated, profile_picture_url, created_at)
        VALUES (?, ?, ?, ?, ?, 1, '', ?)
    `).run(testUserId, 'Test User', 'test@example.com', testPasswordHash, testToken, now);

    db.prepare(`
        INSERT INTO users (id, name, email, password_hash, token, activated, profile_picture_url, created_at)
        VALUES (?, ?, ?, ?, NULL, 1, '', ?)
    `).run(otherUserId, 'Anna Beispiel', 'anna@example.com', bcrypt.hashSync('anna1234', 10), now);

    const insertArticle = db.prepare(`
        INSERT INTO articles (user_id, title, description, phone, price, lat, lng, location_name, date, views, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    const seeds = [
        { user: testUserId, title: 'Gleitschirm Advance Alpha 6, Größe M',
            desc: 'Sehr guter Zustand, wenig geflogen. Startgewicht 85-105kg. Wurde in 3 Jahren nur 30 Stunden geflogen. Check aktuell, alle Papiere vorhanden.',
            phone: '0170 1234567', price: 2500, lat: 48.1351, lng: 11.5820, loc: 'München', views: 42,
            colors: [[58, 110, 165], [255, 255, 255]] },
        { user: otherUserId, title: 'Kitesurf Board Naish 138cm',
            desc: 'Twin Tip, 138cm, inklusive Bindungen. Guter Allrounder für Einsteiger bis Fortgeschrittene.',
            phone: '0175 9876543', price: 450, lat: 54.3233, lng: 10.1228, loc: 'Kiel', views: 127,
            colors: [[22, 160, 133], [255, 255, 255]] },
        { user: 'mock-user-3', title: 'Segelflugzeug LS4 Anteil (1/6)',
            desc: 'Anteil an LS4-b, stationiert in Bayreuth. Sehr gepflegt, Halle vorhanden.',
            phone: '0160 2223344', price: 8500, lat: 49.9456, lng: 11.5713, loc: 'Bayreuth', views: 89,
            colors: [[93, 109, 126], [255, 255, 255]] },
        { user: testUserId, title: 'Motorschirm Kit komplett',
            desc: 'Miniplane Top 80, Schirm Dudek Nucleon XX, wenig geflogen. Für Piloten mit BuBiCo.',
            phone: '0179 5551122', price: 4200, lat: 50.1109, lng: 8.6821, loc: 'Frankfurt am Main', views: 56,
            colors: [[230, 126, 34], [255, 255, 255]] },
    ];

    if (!db.prepare('SELECT 1 FROM users WHERE id = ?').get('mock-user-3')) {
        db.prepare(`
            INSERT INTO users (id, name, email, activated, profile_picture_url, created_at)
            VALUES (?, ?, ?, 1, '', ?)
        `).run('mock-user-3', 'Max Mustermann', 'max@example.com', now);
    }

    const insertPicture = db.prepare('INSERT INTO pictures (article_id, filename) VALUES (?, ?)');

    for (const s of seeds) {
        const date = now - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000);
        const result = insertArticle.run(s.user, s.title, s.desc, s.phone, s.price, s.lat, s.lng, s.loc, date, s.views, now);
        const articleId = result.lastInsertRowid;

        const targetName = `article_${articleId}_1.png`;
        fs.writeFileSync(path.join(UPLOADS_DIR, targetName), stripedPng(400, 300, s.colors[0], s.colors[1]));
        insertPicture.run(articleId, targetName);
    }

    db.prepare('INSERT INTO bookmarks (user_id, article_id) VALUES (?, ?)').run(testUserId, 3);

    console.log(`[seed] done. test user token: ${testToken}`);
}

init();
seed();

module.exports = { db, UPLOADS_DIR };
