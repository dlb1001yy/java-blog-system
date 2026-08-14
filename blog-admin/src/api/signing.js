// 纯 JS HMAC-SHA256 实现，兼容 HTTP 非安全上下文（不依赖 crypto.subtle）

const SIGNING_SECRET = 'BlogApiSigningSecret2024!'

// ---------------------------------------------------------------------------
// UTF-8 helpers
// ---------------------------------------------------------------------------
function utf8ToBytes(str) {
  const encoded = encodeURIComponent(str)
  const bytes = []
  for (let i = 0; i < encoded.length; i++) {
    if (encoded[i] === '%') {
      bytes.push(parseInt(encoded.substr(i + 1, 2), 16))
      i += 2
    } else {
      bytes.push(encoded.charCodeAt(i))
    }
  }
  return bytes
}

// ---------------------------------------------------------------------------
// SHA-256 core (operates on a byte array, returns 32-byte digest)
// ---------------------------------------------------------------------------
const K = [
  0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1,
  0x923f82a4, 0xab1c5ed5, 0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
  0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786,
  0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
  0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147,
  0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
  0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b,
  0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
  0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a,
  0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
  0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
]

function rotr(x, n) {
  return (x >>> n) | (x << (32 - n))
}

function sha256Bytes(bytes) {
  const bitLen = bytes.length * 8
  const withOne = bytes.concat([0x80])
  while ((withOne.length % 64) !== 56) {
    withOne.push(0)
  }
  const lenHi = Math.floor(bitLen / 0x100000000)
  const lenLo = bitLen >>> 0
  for (let i = 24; i >= 0; i -= 8) {
    withOne.push((lenHi >>> i) & 0xff)
  }
  for (let i = 24; i >= 0; i -= 8) {
    withOne.push((lenLo >>> i) & 0xff)
  }

  const H = [
    0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
    0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
  ]

  const W = new Array(64)
  for (let off = 0; off < withOne.length; off += 64) {
    for (let i = 0; i < 16; i++) {
      const j = off + i * 4
      W[i] = (withOne[j] << 24) | (withOne[j + 1] << 16) | (withOne[j + 2] << 8) | withOne[j + 3]
      W[i] >>>= 0
    }
    for (let i = 16; i < 64; i++) {
      const s0 = rotr(W[i - 15], 7) ^ rotr(W[i - 15], 18) ^ (W[i - 15] >>> 3)
      const s1 = rotr(W[i - 2], 17) ^ rotr(W[i - 2], 19) ^ (W[i - 2] >>> 10)
      W[i] = (W[i - 16] + s0 + W[i - 7] + s1) >>> 0
    }

    let [a, b, c, d, e, f, g, h] = H

    for (let i = 0; i < 64; i++) {
      const S1 = rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25)
      const ch = (e & f) ^ (~e & g)
      const temp1 = (h + S1 + ch + K[i] + W[i]) >>> 0
      const S0 = rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22)
      const maj = (a & b) ^ (a & c) ^ (b & c)
      const temp2 = (S0 + maj) >>> 0

      h = g
      g = f
      f = e
      e = (d + temp1) >>> 0
      d = c
      c = b
      b = a
      a = (temp1 + temp2) >>> 0
    }

    H[0] = (H[0] + a) >>> 0
    H[1] = (H[1] + b) >>> 0
    H[2] = (H[2] + c) >>> 0
    H[3] = (H[3] + d) >>> 0
    H[4] = (H[4] + e) >>> 0
    H[5] = (H[5] + f) >>> 0
    H[6] = (H[6] + g) >>> 0
    H[7] = (H[7] + h) >>> 0
  }

  const digest = []
  for (let i = 0; i < 8; i++) {
    digest.push((H[i] >>> 24) & 0xff)
    digest.push((H[i] >>> 16) & 0xff)
    digest.push((H[i] >>> 8) & 0xff)
    digest.push(H[i] & 0xff)
  }
  return digest
}

function bytesToHex(bytes) {
  let hex = ''
  for (let i = 0; i < bytes.length; i++) {
    hex += (bytes[i] >>> 4).toString(16)
    hex += (bytes[i] & 0xf).toString(16)
  }
  return hex
}

// ---------------------------------------------------------------------------
// HMAC-SHA256
// ---------------------------------------------------------------------------
function hmacSha256Hex(keyStr, messageStr) {
  const blockSize = 64
  let keyBytes = utf8ToBytes(keyStr)
  if (keyBytes.length > blockSize) {
    keyBytes = sha256Bytes(keyBytes)
  }
  while (keyBytes.length < blockSize) {
    keyBytes.push(0)
  }

  const oKeyPad = []
  const iKeyPad = []
  for (let i = 0; i < blockSize; i++) {
    oKeyPad.push(keyBytes[i] ^ 0x5c)
    iKeyPad.push(keyBytes[i] ^ 0x36)
  }

  const messageBytes = utf8ToBytes(messageStr)
  const innerHash = sha256Bytes(iKeyPad.concat(messageBytes))
  const outerHash = sha256Bytes(oKeyPad.concat(innerHash))
  return bytesToHex(outerHash)
}

// ---------------------------------------------------------------------------
// Request signing API（同步，无需 crypto.subtle）
// ---------------------------------------------------------------------------
function generateNonce() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2)
}

export function signRequest(method, url) {
  const timestamp = Date.now().toString()
  const nonce = generateNonce()
  const uri = url.startsWith('/api') ? url : '/api' + url
  const stringToSign = [method.toUpperCase(), uri, timestamp, nonce].join('\n')
  const signature = hmacSha256Hex(SIGNING_SECRET, stringToSign)
  return { timestamp, nonce, signature }
}
