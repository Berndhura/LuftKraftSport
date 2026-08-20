const express = require('express');
const cors = require('cors');
const morgan = require('morgan');

require('./db');

const app = express();

app.use(morgan('dev'));
app.use(cors());

app.get('/health', (_req, res) => res.json({ ok: true }));

const api = express.Router();
api.use('/articles', require('./routes/articles').router);
api.use('/bookmarks', require('./routes/bookmarks'));
api.use('/users', require('./routes/users'));
api.use('/messages', require('./routes/messages'));
api.use('/searches', require('./routes/searches'));
api.use('/pictures', require('./routes/pictures'));

app.use('/api/V3', api);

app.use((err, _req, res, _next) => {
    console.error('[error]', err);
    res.status(500).json({ error: err.message || 'internal error' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`LKS backend listening on http://localhost:${PORT}`);
    console.log(`  Emulator base URL: http://10.0.2.2:${PORT}/api/V3/`);
});
