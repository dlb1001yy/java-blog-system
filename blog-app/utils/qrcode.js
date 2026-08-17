// 零依赖 QR Code Model 2 二维码生成器（纯 JS 实现，算法参考 Kazuhiko Arase 的 qrcode-generator）
// 用于 Canvas 绘制文章分享海报二维码，无任何外部依赖，兼容 uni-app 旧版 canvas context 与标准 Canvas。
// 用法：
//   const matrix = createQrMatrix('https://example.com/a/1', { ecc: 'M' })
//   drawQrToCanvas(ctx, matrix, x, y, size, { dark: '#0F172A' })

// ---------- GF(256) 有限域运算（本原多项式 x^8+x^4+x^3+x^2+1，即 0x11d） ----------
const GF_EXP = new Array(256)
const GF_LOG = new Array(256)
for (let i = 0; i < 8; i++) GF_EXP[i] = 1 << i
for (let i = 8; i < 256; i++) GF_EXP[i] = GF_EXP[i - 4] ^ GF_EXP[i - 5] ^ GF_EXP[i - 6] ^ GF_EXP[i - 8]
for (let i = 0; i < 255; i++) GF_LOG[GF_EXP[i]] = i

// GF(256) 乘法
const gfMul = (a, b) => (a === 0 || b === 0 ? 0 : GF_EXP[(GF_LOG[a] + GF_LOG[b]) % 255])

// RS 生成多项式（动态连乘 (x + α^i)，系数按高次到低次排列，首项恒为 1）
const rsGenPoly = (ecCount) => {
  let poly = [1]
  for (let i = 0; i < ecCount; i++) {
    const next = new Array(poly.length + 1).fill(0)
    for (let j = 0; j < poly.length; j++) {
      next[j] ^= poly[j] // 乘 x
      next[j + 1] ^= gfMul(poly[j], GF_EXP[i]) // 乘 α^i
    }
    poly = next
  }
  return poly
}

// 对数据码字做多项式除法（综合除法取余），返回 ecCount 个纠错码字
const rsEncode = (data, ecCount) => {
  const gen = rsGenPoly(ecCount)
  const rem = data.concat(new Array(ecCount).fill(0))
  for (let i = 0; i < data.length; i++) {
    const factor = rem[i]
    if (factor !== 0) {
      for (let j = 0; j < gen.length; j++) rem[i + j] ^= gfMul(gen[j], factor)
    }
  }
  return rem.slice(data.length)
}

// ---------- BCH 纠错计算（格式信息 / 版本信息） ----------
const G15 = 0x537 // 格式信息生成多项式
const G15_MASK = 0x5412 // 格式信息异或掩码
const G18 = 0x1f25 // 版本信息生成多项式

// 二进制有效位数
const bitLength = (n) => {
  let d = 0
  while (n !== 0) {
    d++
    n >>>= 1
  }
  return d
}

// 5bit 数据（纠错等级 2bit + 掩码 3bit）→ 15bit 格式信息（BCH(15,5) + 0x5412 异或）
const bchFormat = (data) => {
  let d = data << 10
  while (bitLength(d) - bitLength(G15) >= 0) d ^= G15 << (bitLength(d) - bitLength(G15))
  return ((data << 10) | d) ^ G15_MASK
}

// 6bit 版本号 → 18bit 版本信息（BCH(18,6)）
const bchVersion = (data) => {
  let d = data << 12
  while (bitLength(d) - bitLength(G18) >= 0) d ^= G18 << (bitLength(d) - bitLength(G18))
  return (data << 12) | d
}

// ---------- 纠错等级与 RS 块结构表 ----------
// formatBits 为格式信息中的 2bit 等级指示：L=01 M=00 Q=11 H=10；col 为块结构表列序（L/M/Q/H）
const ECC_LEVELS = {
  L: { col: 0, formatBits: 1 },
  M: { col: 1, formatBits: 0 },
  Q: { col: 2, formatBits: 3 },
  H: { col: 3, formatBits: 2 }
}

// RS 块结构表（版本 1~40 × L/M/Q/H），每项为：
// [每块纠错码字数, 组1块数, 组1每块数据码字数, 组2块数, 组2每块数据码字数]
const RS_BLOCKS = [
  /* 1 */ [[7,1,19,0,0],[10,1,16,0,0],[13,1,13,0,0],[17,1,9,0,0]],
  /* 2 */ [[10,1,34,0,0],[16,1,28,0,0],[22,1,22,0,0],[28,1,16,0,0]],
  /* 3 */ [[15,1,55,0,0],[26,1,44,0,0],[18,2,17,0,0],[22,2,13,0,0]],
  /* 4 */ [[20,1,80,0,0],[18,2,32,0,0],[26,2,24,0,0],[16,4,9,0,0]],
  /* 5 */ [[26,1,108,0,0],[24,2,43,0,0],[18,2,15,2,16],[22,2,11,2,12]],
  /* 6 */ [[18,2,68,0,0],[16,4,27,0,0],[24,4,19,0,0],[28,4,15,0,0]],
  /* 7 */ [[20,2,78,0,0],[18,4,31,0,0],[18,2,14,4,15],[26,4,13,1,14]],
  /* 8 */ [[24,2,97,0,0],[18,2,38,2,39],[22,4,18,2,19],[26,4,14,2,15]],
  /* 9 */ [[30,2,116,0,0],[22,3,36,2,37],[22,4,16,4,17],[26,4,12,4,13]],
  /* 10 */ [[18,2,68,2,69],[26,4,43,1,44],[24,6,19,2,20],[28,6,15,2,16]],
  /* 11 */ [[20,4,81,0,0],[30,1,50,4,51],[28,4,22,4,23],[24,3,12,8,13]],
  /* 12 */ [[24,2,92,2,93],[22,6,36,2,37],[26,4,20,6,21],[28,7,14,4,15]],
  /* 13 */ [[26,4,107,0,0],[22,8,37,1,38],[24,8,20,4,21],[22,12,11,4,12]],
  /* 14 */ [[30,3,115,1,116],[24,4,40,5,41],[20,11,16,5,17],[24,11,12,5,13]],
  /* 15 */ [[22,5,87,1,88],[24,5,41,5,42],[30,5,24,7,25],[24,11,12,7,13]],
  /* 16 */ [[24,5,98,1,99],[28,7,45,3,46],[24,15,19,2,20],[30,3,15,13,16]],
  /* 17 */ [[28,1,107,5,108],[28,10,46,1,47],[28,1,22,15,23],[28,2,14,17,15]],
  /* 18 */ [[30,5,120,1,121],[26,9,43,4,44],[28,17,22,1,23],[28,2,14,19,15]],
  /* 19 */ [[28,3,113,4,114],[26,3,44,11,45],[26,17,21,4,22],[26,9,13,16,14]],
  /* 20 */ [[28,3,107,5,108],[26,3,41,13,42],[30,15,24,5,25],[28,15,15,10,16]],
  /* 21 */ [[28,4,116,4,117],[26,17,42,0,0],[28,17,22,6,23],[30,19,16,6,17]],
  /* 22 */ [[28,2,111,7,112],[28,17,46,0,0],[30,7,24,16,25],[24,34,13,0,0]],
  /* 23 */ [[30,4,121,5,122],[28,4,47,14,48],[30,11,24,14,25],[30,16,15,14,16]],
  /* 24 */ [[30,6,117,4,118],[28,6,45,14,46],[30,11,24,16,25],[30,30,16,2,17]],
  /* 25 */ [[26,8,106,4,107],[28,8,47,13,48],[30,7,24,22,25],[30,22,15,13,16]],
  /* 26 */ [[28,10,114,2,115],[28,19,46,4,47],[28,28,22,6,23],[30,33,16,4,17]],
  /* 27 */ [[30,8,122,4,123],[28,22,45,3,46],[30,8,23,26,24],[30,12,15,28,16]],
  /* 28 */ [[30,3,117,10,118],[28,3,45,23,46],[30,4,24,31,25],[30,11,15,31,16]],
  /* 29 */ [[30,7,116,7,117],[28,21,45,7,46],[30,1,23,37,24],[30,19,15,26,16]],
  /* 30 */ [[30,5,115,10,116],[28,19,47,10,48],[30,15,24,25,25],[30,23,15,25,16]],
  /* 31 */ [[30,13,115,3,116],[28,2,46,29,47],[30,42,24,1,25],[30,23,15,28,16]],
  /* 32 */ [[30,17,115,0,0],[28,10,46,23,47],[30,10,24,35,25],[30,19,15,35,16]],
  /* 33 */ [[30,17,115,1,116],[28,14,46,21,47],[30,29,24,19,25],[30,11,15,46,16]],
  /* 34 */ [[30,13,115,6,116],[28,14,46,23,47],[30,44,24,7,25],[30,59,16,1,17]],
  /* 35 */ [[30,12,121,7,122],[28,12,47,26,48],[30,39,24,14,25],[30,22,15,41,16]],
  /* 36 */ [[30,6,121,14,122],[28,6,47,34,48],[30,46,24,10,25],[30,2,15,64,16]],
  /* 37 */ [[30,17,122,4,123],[28,29,46,14,47],[30,49,24,10,25],[30,24,15,46,16]],
  /* 38 */ [[30,4,122,18,123],[28,13,46,32,47],[30,48,24,14,25],[30,42,15,32,16]],
  /* 39 */ [[30,20,117,4,118],[28,40,47,7,48],[30,43,24,22,25],[30,10,15,67,16]],
  /* 40 */ [[30,19,118,6,119],[28,18,47,31,48],[30,34,24,34,25],[30,20,15,61,16]]
]

// 指定版本 + 纠错等级的总数据码字数
const dataCodewords = (version, col) => {
  const [, b1, d1, b2, d2] = RS_BLOCKS[version - 1][col]
  return b1 * d1 + b2 * d2
}

// alignment 图案中心坐标（版本 1 无；其余按 ISO 18004 标准间距算法计算，v32 特例 step=26）
const alignmentCenters = (version) => {
  if (version === 1) return []
  const size = version * 4 + 17
  const num = Math.floor(version / 7) + 2
  const step = version === 32 ? 26 : Math.floor((version * 4 + num * 2 + 1) / (num * 2 - 2)) * 2
  const centers = []
  for (let i = 0; i < num - 1; i++) centers.push(size - 7 - (num - 2 - i) * step)
  return [6].concat(centers)
}

// ---------- UTF-8 编码（按码点逐字符转字节） ----------
const toUtf8 = (text) => {
  const bytes = []
  for (const ch of String(text)) {
    const cp = ch.codePointAt(0)
    if (cp < 0x80) bytes.push(cp)
    else if (cp < 0x800) bytes.push(0xc0 | (cp >> 6), 0x80 | (cp & 0x3f))
    else if (cp < 0x10000) bytes.push(0xe0 | (cp >> 12), 0x80 | ((cp >> 6) & 0x3f), 0x80 | (cp & 0x3f))
    else bytes.push(0xf0 | (cp >> 18), 0x80 | ((cp >> 12) & 0x3f), 0x80 | ((cp >> 6) & 0x3f), 0x80 | (cp & 0x3f))
  }
  return bytes
}

// ---------- 数据位流编码 + 分块 RS 纠错 + 交织 ----------
const buildCodewords = (bytes, version, col) => {
  const [ecCount, b1, d1, b2, d2] = RS_BLOCKS[version - 1][col]
  const capacity = b1 * d1 + b2 * d2 // 总数据码字数
  const bits = []
  const put = (num, len) => {
    for (let i = len - 1; i >= 0; i--) bits.push((num >>> i) & 1) // 高位在前
  }
  put(4, 4) // 模式指示符 0100：字节模式
  put(bytes.length, version <= 9 ? 8 : 16) // 字符数指示符：v1-9 为 8bit，v10-40 为 16bit
  for (const b of bytes) put(b, 8)
  for (let i = 0; i < 4 && bits.length < capacity * 8; i++) bits.push(0) // 终止符 0000（容量不足可截断）
  while (bits.length % 8 !== 0) bits.push(0) // 补齐字节边界
  const data = []
  for (let i = 0; i < bits.length; i += 8) {
    let byte = 0
    for (let j = 0; j < 8; j++) byte = (byte << 1) | bits[i + j]
    data.push(byte)
  }
  for (let alt = true; data.length < capacity; alt = !alt) data.push(alt ? 0xec : 0x11) // 交替填充 0xEC / 0x11
  // 分块（组1 / 组2）并计算各块 RS 纠错码字
  const blocks = []
  let offset = 0
  for (const [count, dc] of [[b1, d1], [b2, d2]]) {
    for (let i = 0; i < count; i++) {
      const chunk = data.slice(offset, offset + dc)
      offset += dc
      blocks.push({ data: chunk, ec: rsEncode(chunk, ecCount) })
    }
  }
  // 交织：数据码字按列 zigzag 交错（短块跳过）；纠错码字按块顺序交错
  const out = []
  const maxData = Math.max(d1, d2)
  for (let i = 0; i < maxData; i++) for (const b of blocks) if (i < b.data.length) out.push(b.data[i])
  for (let i = 0; i < ecCount; i++) for (const b of blocks) out.push(b.ec[i])
  return out
}

// ---------- 8 种标准掩码公式（r=行，c=列） ----------
const maskBit = (mask, r, c) => {
  switch (mask) {
    case 0: return (r + c) % 2 === 0
    case 1: return r % 2 === 0
    case 2: return c % 3 === 0
    case 3: return (r + c) % 3 === 0
    case 4: return (Math.floor(r / 2) + Math.floor(c / 3)) % 2 === 0
    case 5: return (r * c) % 2 + (r * c) % 3 === 0
    case 6: return ((r * c) % 2 + (r * c) % 3) % 2 === 0
    default: return ((r * c) % 3 + (r + c) % 2) % 2 === 0
  }
}

// 掩码惩罚评分（ISO/IEC 18004）：N1=3（连续同色）N2=3（2x2 块）N3=40（类 finder）N4=10（暗比例）
const penaltyScore = (matrix) => {
  const size = matrix.length
  let score = 0
  // 提取全部行/列的二进制串，便于规则 1 / 3 扫描
  const lines = []
  for (let r = 0; r < size; r++) lines.push(matrix[r].map((v) => (v ? '1' : '0')).join(''))
  for (let c = 0; c < size; c++) {
    let s = ''
    for (let r = 0; r < size; r++) s += matrix[r][c] ? '1' : '0'
    lines.push(s)
  }
  for (const line of lines) {
    // 规则1：行/列连续同色 >=5 个模块，罚 3 + (连长 - 5)
    const runs = line.match(/0{5,}|1{5,}/g) || []
    for (const run of runs) score += 3 + run.length - 5
    // 规则3：1011101 且一侧紧邻 4 个浅色模块（类似 finder），罚 40（支持重叠匹配）
    let i = line.indexOf('00001011101')
    while (i !== -1) {
      score += 40
      i = line.indexOf('00001011101', i + 1)
    }
    i = line.indexOf('10111010000')
    while (i !== -1) {
      score += 40
      i = line.indexOf('10111010000', i + 1)
    }
  }
  // 规则2：2x2 同色块，每块罚 3
  for (let r = 0; r < size - 1; r++) {
    for (let c = 0; c < size - 1; c++) {
      const v = matrix[r][c]
      if (v === matrix[r][c + 1] && v === matrix[r + 1][c] && v === matrix[r + 1][c + 1]) score += 3
    }
  }
  // 规则4：暗模块比例偏离 50%，每 5% 罚 10
  let darkCount = 0
  for (let r = 0; r < size; r++) for (let c = 0; c < size; c++) if (matrix[r][c]) darkCount++
  score += Math.floor(Math.abs((darkCount * 100) / (size * size) - 50) / 5) * 10
  return score
}

// ---------- 构建完整矩阵（功能图形 + 掩码数据） ----------
const buildMatrix = (version, level, codewords, mask) => {
  const size = version * 4 + 17
  const modules = []
  const reserved = [] // 功能图形占位标记，数据放置时跳过
  for (let r = 0; r < size; r++) {
    modules.push(new Array(size).fill(false))
    reserved.push(new Array(size).fill(false))
  }
  const set = (r, c, dark) => {
    modules[r][c] = dark
    reserved[r][c] = true
  }
  // 1. finder 图案（左上/右上/左下，外圈 1 模块浅色分隔）
  const finder = (row, col) => {
    for (let r = row - 1; r <= row + 7; r++) {
      for (let c = col - 1; c <= col + 7; c++) {
        if (r < 0 || r >= size || c < 0 || c >= size) continue
        const dark = (r >= row && r <= row + 6 && (c === col || c === col + 6)) ||
          (c >= col && c <= col + 6 && (r === row || r === row + 6)) ||
          (r >= row + 2 && r <= row + 4 && c >= col + 2 && c <= col + 4)
        set(r, c, dark)
      }
    }
  }
  finder(0, 0)
  finder(0, size - 7)
  finder(size - 7, 0)
  // 2. alignment 图案（5x5：外环深、中环浅、中心深；跳过与 finder 重叠的组合，先于 timing 绘制）
  const centers = alignmentCenters(version)
  for (const r of centers) {
    for (const c of centers) {
      if (reserved[r][c]) continue
      for (let dr = -2; dr <= 2; dr++) {
        for (let dc = -2; dc <= 2; dc++) {
          set(r + dr, c + dc, Math.abs(dr) === 2 || Math.abs(dc) === 2 || (dr === 0 && dc === 0))
        }
      }
    }
  }
  // 3. timing 图案（第 6 行/列，明暗交替，跳过已占用模块）
  for (let i = 8; i < size - 8; i++) {
    if (!reserved[i][6]) set(i, 6, i % 2 === 0)
    if (!reserved[6][i]) set(6, i, i % 2 === 0)
  }
  // 4. 格式信息（15bit 两处副本，LSB 在前放置）+ 固定 dark module
  const fmt = bchFormat((level.formatBits << 3) | mask)
  for (let i = 0; i < 15; i++) {
    const bit = ((fmt >> i) & 1) === 1
    if (i < 6) set(i, 8, bit) // 竖直副本（col 8）：rows 0-5
    else if (i < 8) set(i + 1, 8, bit) // rows 7-8（跳过第 6 行 timing）
    else set(size - 15 + i, 8, bit) // 左下副本：rows size-7 ~ size-1
    if (i < 8) set(8, size - 1 - i, bit) // 水平副本（row 8）：右上 cols size-1 ~ size-8
    else if (i === 8) set(8, 7, bit) // col 7（跳过第 6 列 timing）
    else set(8, 14 - i, bit) // 左上副本：cols 5 ~ 0
  }
  set(size - 8, 8, true) // 固定 dark module：(row 4V+9, col 8)
  // 5. 版本信息（v>=7，BCH(18,6)，两处 3x6 / 6x3）
  if (version >= 7) {
    const vb = bchVersion(version)
    for (let i = 0; i < 18; i++) {
      const bit = ((vb >> i) & 1) === 1
      set(Math.floor(i / 3), size - 11 + (i % 3), bit) // 右上 3x6
      set(size - 11 + (i % 3), Math.floor(i / 3), bit) // 左下 6x3
    }
  }
  // 6. 数据放置：从右下角起，两列一组向上/向下蛇形；遇第 6 列竖直整体跳过一列；每字节高位在前
  let row = size - 1
  let dir = -1
  let bitIndex = 7
  let byteIndex = 0
  for (let col = size - 1; col > 0; col -= 2) {
    if (col === 6) col-- // 跳过 timing 竖列
    while (true) {
      for (let c = 0; c < 2; c++) {
        if (!reserved[row][col - c]) {
          let dark = false
          if (byteIndex < codewords.length) dark = ((codewords[byteIndex] >>> bitIndex) & 1) === 1
          if (maskBit(mask, row, col - c)) dark = !dark
          modules[row][col - c] = dark
          if (--bitIndex === -1) {
            byteIndex++
            bitIndex = 7
          }
        }
      }
      row += dir
      if (row < 0 || row >= size) {
        row -= dir
        dir = -dir
        break
      }
    }
  }
  return modules
}

/**
 * 生成二维码矩阵（QR Code Model 2，字节模式，UTF-8 编码，自动选择最小可用版本 1~40）
 * @param {string} text 二维码内容
 * @param {Object} options { ecc = 'M' } 纠错等级 'L' | 'M' | 'Q' | 'H'
 * @returns {Array<Array<boolean>>} matrix[row][col]，true 表示黑色模块
 */
export const createQrMatrix = (text, options = {}) => {
  const ecc = options && options.ecc ? options.ecc : 'M'
  const level = ECC_LEVELS[ecc]
  if (!level) throw new Error(`无效纠错等级 ${ecc}，仅支持 L/M/Q/H`)
  const bytes = toUtf8(text == null ? '' : text)
  // 选择能容纳数据的最小版本（模式 4bit + 字符数指示符 + 数据字节）
  let version = 0
  for (let v = 1; v <= 40; v++) {
    const countBits = v <= 9 ? 8 : 16
    if (4 + countBits + bytes.length * 8 <= dataCodewords(v, level.col) * 8) {
      version = v
      break
    }
  }
  if (!version) throw new Error(`二维码内容过长（${bytes.length} 字节），超出版本 40 容量上限`)
  const codewords = buildCodewords(bytes, version, level.col)
  // 8 种掩码全部构建评分，取惩罚分最低者
  let best = null
  let bestScore = Infinity
  for (let mask = 0; mask < 8; mask++) {
    const matrix = buildMatrix(version, level, codewords, mask)
    const score = penaltyScore(matrix)
    if (score < bestScore) {
      bestScore = score
      best = matrix
    }
  }
  return best
}

/**
 * 将二维码矩阵绘制到 Canvas（兼容 uni.createCanvasContext 与标准 CanvasRenderingContext2D）
 * @param {Object} ctx canvas 上下文
 * @param {Array<Array<boolean>>} matrix createQrMatrix 返回的矩阵
 * @param {number} x 目标区域左上角 x
 * @param {number} y 目标区域左上角 y
 * @param {number} size 目标区域边长（正方形）
 * @param {Object} options { dark = '#0F172A', light = '#FFFFFF', margin = true } margin 为 true 时四周补 2 模块静区
 */
export const drawQrToCanvas = (ctx, matrix, x, y, size, options = {}) => {
  const { dark = '#0F172A', light = '#FFFFFF', margin = true } = options
  const n = matrix.length
  const m = size / (n + (margin ? 4 : 0)) // 单模块边长（允许浮点）
  const off = margin ? m * 2 : 0
  // 先整体绘制浅色底（含静区）
  ctx.fillStyle = light
  ctx.fillRect(x, y, size, size)
  // 再逐模块绘制深色（浮点坐标；+0.02 微重叠，避免设备像素缝隙出现细白线）
  ctx.fillStyle = dark
  for (let r = 0; r < n; r++) {
    for (let c = 0; c < n; c++) {
      if (matrix[r][c]) ctx.fillRect(x + off + c * m, y + off + r * m, m + 0.02, m + 0.02)
    }
  }
}

export default { createQrMatrix, drawQrToCanvas }
