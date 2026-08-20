const express = require('express');
const path = require('path');
const fs = require('fs');
const { db, UPLOADS_DIR } = require('../db');

const router = express.Router();

function sendPicture(req, res) {
    const pic = db.prepare('SELECT filename FROM pictures WHERE id = ?').get(req.params.id);
    if (!pic) return res.status(404).end();
    const filePath = path.join(UPLOADS_DIR, pic.filename);
    if (!fs.existsSync(filePath)) return res.status(404).end();
    res.sendFile(filePath);
}

router.get('/:id/thumbnail', sendPicture);
router.get('/:id', sendPicture);

module.exports = router;
