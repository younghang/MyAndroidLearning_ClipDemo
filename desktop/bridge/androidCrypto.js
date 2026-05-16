const crypto = require("crypto");

const END_FLAGS = [0x80000000 | 0, 0x800000, 0x8000, 0x80];
const RIGHT1 = [0, 40, 48, 56];
const RIGHT2 = [0, 8, 16, 24];
const LEFT = [0, 24, 16, 8];
const MASK_BIG = [0xffffffffn, 0x00ffffffn, 0x0000ffffn, 0x000000ffn];
const HASHBYTES_TO_USE = 20;
const FRAME_LENGTH = 16;
const COUNTER_BASE = 0n;
const HASHCOPY_OFFSET = 0;
const EXTRAFRAME_OFFSET = 5;
const FRAME_OFFSET = 21;
const MAX_BYTES = 48;
const UNDEFINED = 0;
const SET_SEED = 1;
const NEXT_BYTES = 2;
const H0 = 0x67452301 | 0;
const H1 = 0xefcdab89 | 0;
const H2 = 0x98badcfe | 0;
const H3 = 0x10325476 | 0;
const H4 = 0xc3d2e1f0 | 0;
const BYTES_OFFSET = 81;
const HASH_OFFSET = 82;
const DIGEST_LENGTH = 20;
const KEY_SIZE = 32;

function deriveKey(password) {
  const derivator = new InsecureSHA1PRNGKeyDerivator();
  derivator.setSeed(Buffer.from(String(password), "utf8"));
  return derivator.nextBytes(KEY_SIZE);
}

function encryptAndroidText(clearText, password) {
  const key = deriveKey(password);
  const cipher = crypto.createCipheriv("aes-256-ecb", key, null);
  cipher.setAutoPadding(true);
  return Buffer.concat([
    cipher.update(String(clearText), "utf8"),
    cipher.final(),
  ]).toString("hex").toUpperCase();
}

function decryptAndroidText(encryptedHex, password) {
  const key = deriveKey(password);
  const decipher = crypto.createDecipheriv("aes-256-ecb", key, null);
  decipher.setAutoPadding(true);
  return Buffer.concat([
    decipher.update(Buffer.from(String(encryptedHex).trim(), "hex")),
    decipher.final(),
  ]).toString("utf8");
}

class InsecureSHA1PRNGKeyDerivator {
  constructor() {
    this.seed = new Array(HASH_OFFSET + EXTRAFRAME_OFFSET).fill(0);
    this.seed[HASH_OFFSET] = H0;
    this.seed[HASH_OFFSET + 1] = H1;
    this.seed[HASH_OFFSET + 2] = H2;
    this.seed[HASH_OFFSET + 3] = H3;
    this.seed[HASH_OFFSET + 4] = H4;
    this.seedLength = 0;
    this.copies = new Array(2 * FRAME_LENGTH + EXTRAFRAME_OFFSET).fill(0);
    this.nextBuffer = Buffer.alloc(DIGEST_LENGTH);
    this.nextBIndex = HASHBYTES_TO_USE;
    this.counter = COUNTER_BASE;
    this.state = UNDEFINED;
  }

  setSeed(seed) {
    if (!seed) throw new Error("seed == null");
    if (this.state === NEXT_BYTES) {
      arrayCopy(this.copies, HASHCOPY_OFFSET, this.seed, HASH_OFFSET, EXTRAFRAME_OFFSET);
    }
    this.state = SET_SEED;
    if (seed.length !== 0) {
      updateHash(this.seed, seed, 0, seed.length - 1);
      this.seedLength += seed.length;
    }
  }

  nextBytes(length) {
    const bytes = Buffer.alloc(length);
    let i;
    let n;
    let nextByteToReturn;
    let lastWord;
    const extrabytes = 7;
    lastWord = this.seed[BYTES_OFFSET] === 0 ? 0 : (this.seed[BYTES_OFFSET] + extrabytes) >> (3 - 1);
    if (this.state === UNDEFINED) {
      throw new Error("No seed supplied!");
    } else if (this.state === SET_SEED) {
      arrayCopy(this.seed, HASH_OFFSET, this.copies, HASHCOPY_OFFSET, EXTRAFRAME_OFFSET);
      for (i = lastWord + 3; i < FRAME_LENGTH + 2; i++) {
        this.seed[i] = 0;
      }
      const bits = BigInt(this.seedLength) * 8n + 64n;
      if (this.seed[BYTES_OFFSET] < MAX_BYTES) {
        this.seed[14] = Number((bits >> 32n) & 0xffffffffn) | 0;
        this.seed[15] = Number(bits & 0xffffffffn) | 0;
      } else {
        this.copies[EXTRAFRAME_OFFSET + 14] = Number((bits >> 32n) & 0xffffffffn) | 0;
        this.copies[EXTRAFRAME_OFFSET + 15] = Number(bits & 0xffffffffn) | 0;
      }
      this.nextBIndex = HASHBYTES_TO_USE;
    }
    this.state = NEXT_BYTES;
    if (bytes.length === 0) return bytes;
    nextByteToReturn = 0;
    n = (HASHBYTES_TO_USE - this.nextBIndex) < (bytes.length - nextByteToReturn)
      ? HASHBYTES_TO_USE - this.nextBIndex
      : bytes.length - nextByteToReturn;
    if (n > 0) {
      this.nextBuffer.copy(bytes, nextByteToReturn, this.nextBIndex, this.nextBIndex + n);
      this.nextBIndex += n;
      nextByteToReturn += n;
    }
    if (nextByteToReturn >= bytes.length) return bytes;
    n = this.seed[BYTES_OFFSET] & 0x03;
    for (;;) {
      if (n === 0) {
        this.seed[lastWord] = Number((this.counter >> 32n) & 0xffffffffn) | 0;
        this.seed[lastWord + 1] = Number(this.counter & 0xffffffffn) | 0;
        this.seed[lastWord + 2] = END_FLAGS[0];
      } else {
        this.seed[lastWord] = (this.seed[lastWord] | Number((this.counter >> BigInt(RIGHT1[n])) & MASK_BIG[n])) | 0;
        this.seed[lastWord + 1] = Number((this.counter >> BigInt(RIGHT2[n])) & 0xffffffffn) | 0;
        this.seed[lastWord + 2] = (Number((this.counter << BigInt(LEFT[n])) & 0xffffffffn) | END_FLAGS[n]) | 0;
      }
      if (this.seed[BYTES_OFFSET] > MAX_BYTES) {
        this.copies[EXTRAFRAME_OFFSET] = this.seed[FRAME_LENGTH];
        this.copies[EXTRAFRAME_OFFSET + 1] = this.seed[FRAME_LENGTH + 1];
      }
      computeHash(this.seed);
      if (this.seed[BYTES_OFFSET] > MAX_BYTES) {
        arrayCopy(this.seed, 0, this.copies, FRAME_OFFSET, FRAME_LENGTH);
        arrayCopy(this.copies, EXTRAFRAME_OFFSET, this.seed, 0, FRAME_LENGTH);
        computeHash(this.seed);
        arrayCopy(this.copies, FRAME_OFFSET, this.seed, 0, FRAME_LENGTH);
      }
      this.counter++;
      let j = 0;
      for (i = 0; i < EXTRAFRAME_OFFSET; i++) {
        const k = this.seed[HASH_OFFSET + i];
        this.nextBuffer[j] = (k >>> 24) & 0xff;
        this.nextBuffer[j + 1] = (k >>> 16) & 0xff;
        this.nextBuffer[j + 2] = (k >>> 8) & 0xff;
        this.nextBuffer[j + 3] = k & 0xff;
        j += 4;
      }
      this.nextBIndex = 0;
      j = HASHBYTES_TO_USE < (bytes.length - nextByteToReturn)
        ? HASHBYTES_TO_USE
        : bytes.length - nextByteToReturn;
      if (j > 0) {
        this.nextBuffer.copy(bytes, nextByteToReturn, 0, j);
        nextByteToReturn += j;
        this.nextBIndex += j;
      }
      if (nextByteToReturn >= bytes.length) break;
    }
    return bytes;
  }
}

function computeHash(arrW) {
  let a = arrW[HASH_OFFSET];
  let b = arrW[HASH_OFFSET + 1];
  let c = arrW[HASH_OFFSET + 2];
  let d = arrW[HASH_OFFSET + 3];
  let e = arrW[HASH_OFFSET + 4];
  let temp;
  for (let t = 16; t < 80; t++) {
    temp = arrW[t - 3] ^ arrW[t - 8] ^ arrW[t - 14] ^ arrW[t - 16];
    arrW[t] = ((temp << 1) | (temp >>> 31)) | 0;
  }
  for (let t = 0; t < 20; t++) {
    temp = add32(rotl(a, 5), (b & c) | ((~b) & d), e, arrW[t], 0x5a827999);
    e = d;
    d = c;
    c = rotl(b, 30);
    b = a;
    a = temp;
  }
  for (let t = 20; t < 40; t++) {
    temp = add32(rotl(a, 5), b ^ c ^ d, e, arrW[t], 0x6ed9eba1);
    e = d;
    d = c;
    c = rotl(b, 30);
    b = a;
    a = temp;
  }
  for (let t = 40; t < 60; t++) {
    temp = add32(rotl(a, 5), (b & c) | (b & d) | (c & d), e, arrW[t], 0x8f1bbcdc);
    e = d;
    d = c;
    c = rotl(b, 30);
    b = a;
    a = temp;
  }
  for (let t = 60; t < 80; t++) {
    temp = add32(rotl(a, 5), b ^ c ^ d, e, arrW[t], 0xca62c1d6);
    e = d;
    d = c;
    c = rotl(b, 30);
    b = a;
    a = temp;
  }
  arrW[HASH_OFFSET] = add32(arrW[HASH_OFFSET], a);
  arrW[HASH_OFFSET + 1] = add32(arrW[HASH_OFFSET + 1], b);
  arrW[HASH_OFFSET + 2] = add32(arrW[HASH_OFFSET + 2], c);
  arrW[HASH_OFFSET + 3] = add32(arrW[HASH_OFFSET + 3], d);
  arrW[HASH_OFFSET + 4] = add32(arrW[HASH_OFFSET + 4], e);
}

function updateHash(intArray, byteInput, fromByte, toByte) {
  let index = intArray[BYTES_OFFSET];
  let i = fromByte;
  let wordIndex = index >> 2;
  let byteIndex = index & 0x03;
  intArray[BYTES_OFFSET] = (index + toByte - fromByte + 1) & 0x3f;
  if (byteIndex !== 0) {
    for (; i <= toByte && byteIndex < 4; i++) {
      intArray[wordIndex] = (intArray[wordIndex] | ((byteInput[i] & 0xff) << ((3 - byteIndex) << 3))) | 0;
      byteIndex++;
    }
    if (byteIndex === 4) {
      wordIndex++;
      if (wordIndex === 16) {
        computeHash(intArray);
        wordIndex = 0;
      }
    }
    if (i > toByte) return;
  }
  const maxWord = (toByte - i + 1) >> 2;
  for (let k = 0; k < maxWord; k++) {
    intArray[wordIndex] = (((byteInput[i] & 0xff) << 24)
      | ((byteInput[i + 1] & 0xff) << 16)
      | ((byteInput[i + 2] & 0xff) << 8)
      | (byteInput[i + 3] & 0xff)) | 0;
    i += 4;
    wordIndex++;
    if (wordIndex < 16) continue;
    computeHash(intArray);
    wordIndex = 0;
  }
  const nBytes = toByte - i + 1;
  if (nBytes !== 0) {
    let w = (byteInput[i] & 0xff) << 24;
    if (nBytes !== 1) {
      w |= (byteInput[i + 1] & 0xff) << 16;
      if (nBytes !== 2) {
        w |= (byteInput[i + 2] & 0xff) << 8;
      }
    }
    intArray[wordIndex] = w | 0;
  }
}

function rotl(value, bits) {
  return ((value << bits) | (value >>> (32 - bits))) | 0;
}

function add32(...values) {
  let result = 0;
  values.forEach((value) => {
    result = (result + value) | 0;
  });
  return result;
}

function arrayCopy(source, sourceIndex, target, targetIndex, length) {
  for (let i = 0; i < length; i++) {
    target[targetIndex + i] = source[sourceIndex + i];
  }
}

module.exports = {
  encryptAndroidText,
  decryptAndroidText,
  deriveKey,
};
