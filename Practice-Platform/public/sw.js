/**
 * Service Worker —— 离线壳 + 静态资源缓存。
 *
 * 策略分三类，别混：
 *   ① 导航请求（打开页面）：网络优先，断网时回退到缓存的 index.html。
 *      用网络优先是因为 index.html 指向带 hash 的 JS/CSS，
 *      缓存旧壳会和新资源对不上（白屏或报 404）。
 *   ② 同源静态资源（js/css/字体/笔顺数据）：缓存优先。
 *      这些文件名带 hash 或分片名，内容不会变，缓存优先能秒开。
 *   ③ 后端接口与跨域请求：完全不碰，交给网络。
 *
 * 版本号写在下面：改了缓存内容就把 VERSION 加一，
 * 新版本激活时会删掉旧缓存（activate 里清）。
 */

const VERSION = 'v1';
const CACHE_NAME = `calligraphy-shell-${VERSION}`;

// 相对路径：Service Worker 装在站点根，'./' 跟着 BASE_URL 走，
// 部署到子路径（GitHub Pages 的 /仓库名/）也不会指错。
const SHELL = ['./', './index.html'];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then((cache) => cache.addAll(SHELL))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const request = event.request;

  if (request.method !== 'GET') return;

  let url;
  try {
    url = new URL(request.url);
  } catch {
    return;
  }

  // 跨域资源不接管
  if (url.origin !== self.location.origin) return;

  // 后端接口不缓存 —— 作品、字帖、练习记录都是动态数据
  if (url.pathname.includes('/api/')) return;

  // ① 导航请求：网络优先，断网回退缓存的壳
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request).catch(() =>
        caches.match('./index.html').then((hit) => hit || caches.match('./'))
      )
    );
    return;
  }

  // ② 静态资源：缓存优先，命中不到就取网络并顺手存下来
  event.respondWith(
    caches.match(request).then((hit) => {
      if (hit) return hit;
      return fetch(request).then((response) => {
        if (response && response.ok && response.type === 'basic') {
          const copy = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
        }
        return response;
      });
    })
  );
});
