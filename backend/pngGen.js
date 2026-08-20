const zlib = require('zlib');

const CRC_TABLE = (() => {
    const t = new Uint32Array(256);
    for (let i = 0; i < 256; i++) {
        let c = i;
        for (let k = 0; k < 8; k++) c = (c & 1) ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
        t[i] = c >>> 0;
    }
    return t;
})();

function crc32(buf) {
    let c = 0xFFFFFFFF;
    for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xFF] ^ (c >>> 8);
    return (c ^ 0xFFFFFFFF) >>> 0;
}

function chunk(type, data) {
    const typeBuf = Buffer.from(type, 'ascii');
    const len = Buffer.alloc(4); len.writeUInt32BE(data.length, 0);
    const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(Buffer.concat([typeBuf, data])), 0);
    return Buffer.concat([len, typeBuf, data, crc]);
}

/**
 * Renders a simple diagonal-stripe PNG so different seed pictures are
 * visually distinct without needing external image assets.
 */
function stripedPng(width, height, primary, secondary) {
    const signature = Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]);
    const ihdr = Buffer.alloc(13);
    ihdr.writeUInt32BE(width, 0);
    ihdr.writeUInt32BE(height, 4);
    ihdr[8] = 8;   // bit depth
    ihdr[9] = 2;   // color type RGB

    const scanlines = [];
    const stripe = 40;
    for (let y = 0; y < height; y++) {
        const line = Buffer.alloc(1 + width * 3);
        line[0] = 0;
        for (let x = 0; x < width; x++) {
            const useSecondary = Math.floor((x + y) / stripe) % 2 === 0;
            const c = useSecondary ? secondary : primary;
            line[1 + x * 3] = c[0];
            line[2 + x * 3] = c[1];
            line[3 + x * 3] = c[2];
        }
        scanlines.push(line);
    }
    const idat = zlib.deflateSync(Buffer.concat(scanlines));
    return Buffer.concat([signature, chunk('IHDR', ihdr), chunk('IDAT', idat), chunk('IEND', Buffer.alloc(0))]);
}

module.exports = { stripedPng };
