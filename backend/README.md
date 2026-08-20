# LKS local backend

Node/Express + SQLite implementation of the REST endpoints the Android app
consumes. Runs on http://localhost:3000; the Android emulator reaches it via
http://10.0.2.2:3000.

## Setup

```
cd backend
npm install
npm start
```

First start creates `data/lks.db`, seeds it with the four mock articles and one
test user, and copies seed images into `data/uploads/`.

Test account:
- email: `test@example.com`
- password: `test1234`

## Endpoints

Same base path as the original server: `/api/V3/`. All authenticated calls
expect a `?token=` query parameter (matches the existing Retrofit client).

## Reset

```
npm run reset
```

Deletes the DB and uploaded pictures, then re-seeds.
