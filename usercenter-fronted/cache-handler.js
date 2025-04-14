const fs = require('fs');
const path = require('path');
const { promisify } = require('util');

const readFile = promisify(fs.readFile);
const writeFile = promisify(fs.writeFile);
const mkdir = promisify(fs.mkdir);

const CACHE_DIR = path.join(process.cwd(), '.next/cache/custom-cache');

// 确保缓存目录存在
try {
  fs.mkdirSync(CACHE_DIR, { recursive: true });
} catch (error) {
  if (error.code !== 'EEXIST') {
    console.error('Failed to create cache directory:', error);
  }
}

/**
 * 自定义缓存处理器
 */
module.exports = class CustomCacheHandler {
  constructor(options) {
    this.options = options;
  }

  async get(key) {
    try {
      const data = await readFile(path.join(CACHE_DIR, key), 'utf8');
      return JSON.parse(data);
    } catch (error) {
      return null;
    }
  }

  async set(key, data) {
    try {
      await writeFile(
        path.join(CACHE_DIR, key),
        JSON.stringify(data),
        'utf8'
      );
    } catch (error) {
      console.error('Error writing to cache:', error);
    }
  }
}; 