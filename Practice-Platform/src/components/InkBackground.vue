<template>
  <div class="ink-background-container" ref="containerRef">
    <!-- Canvas用于水墨交互效果 -->
    <canvas ref="inkCanvas" class="ink-canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as THREE from 'three';

// ===== 自定义参数配置 =====
// 视觉参数
const visualParams = {
  // 山峰颜色和透明度配置
  mountains: [
    { color: 0xe0e0e0, opacity: 0.3, blur: 3 }, // 最远层：淡灰色远山，模糊处理
    { color: 0xc0c0c0, opacity: 0.4, blur: 2 }, // 远层：浅灰色山峦，轻微模糊
    { color: 0xa0a0a0, opacity: 0.5, blur: 1 }, // 中层：中等灰度山峰
    { color: 0x808080, opacity: 0.7, blur: 0 }, // 近层：深灰色山峰，清晰轮廓
  ],
  // 树木配置
  trees: {
    farTree: { x: -500, y: -50, size: 1, z: -200 },   // 远景树木
    nearTree: { x: 400, y: -100, size: 1.5, z: -50 } // 近景树木
  },
  // 太阳配置
  sun: { x: 600, y: 200, z: -400, color: 0xff9500, size: 60 },
  // 飞鸟配置
  birds: {
    groupCount: 4,
    birdsPerGroup: 6,
    baseSpeed: 1.5,
    speedVariation: 1
  }
};

// 动画参数
const animationParams = {
  // 云雾动画
  clouds: {
    speed: 0.3,
    variation: 0.2,
    heightLevels: [150, 250, 350], // 三层不同高度的云雾
  },
  // 水面波纹
  water: {
    waveFrequency: 0.02,
    waveAmplitude: 2
  },
  // 太阳发光
  sun: {
    glowIntensity: 0.8,
    breathSpeed: 0.02
  },
  // 墨滴扩散
  ink: {
    spreadSpeed: 0.5,
    fadeSpeed: 0.01
  }
};

// 交互参数
const interactionParams = {
  mouse: {
    inkFrequency: 20, // 鼠标移动时墨滴产生频率（每帧概率）
    maxInkRadius: 10  // 鼠标移动产生的墨滴最大半径
  },
  click: {
    inkCount: 3,      // 点击产生的墨滴数量
    maxInkRadius: 20  // 点击产生的墨滴最大半径
  },
  autoInk: {
    frequency: 1000   // 自动墨滴产生频率（毫秒）
  }
};

// ===== 核心变量 =====
const containerRef = ref(null);
const inkCanvas = ref(null);
let scene, camera, renderer, animationId;
let mountains = [];
let clouds = [];
let birds = [];
let sun = null;
let water = null;
// Trees are used in createTree function
const farTree = ref(null);
const nearTree = ref(null);
let inkParticles = [];
let canvasContext = null;
let lastMouseX = 0;
let lastMouseY = 0;
// autoInkTimer is no longer used as auto ink effect is disabled
// let autoInkTimer = null;
let time = 0;
let frameCount = 0;

// ===== 工具函数 =====
// 创建山脉轮廓路径
const createMountainPath = (width, height, pointsCount, irregularity = 0.5) => {
  const path = [];
  for (let i = 0; i <= pointsCount; i++) {
    const x = (i / pointsCount - 0.5) * width;
    // 使用正弦函数和随机值创建自然山脉轮廓
    const y = height * 0.5 * Math.sin(i * 0.8 + Math.random() * 0.3);
    const irregularY = height * 0.5 * Math.random() * irregularity;
    path.push(new THREE.Vector3(x, y + irregularY, 0));
  }
  return path;
};

// 创建不规则多边形
const createIrregularPolygon = (points, baseY) => {
  const shape = new THREE.Shape();

  // 添加山顶点
  shape.moveTo(points[0].x, points[0].y);
  for (let i = 1; i < points.length; i++) {
    shape.lineTo(points[i].x, points[i].y);
  }

  // 添加底部点
  shape.lineTo(points[points.length - 1].x, baseY);
  shape.lineTo(points[0].x, baseY);
  shape.closePath();

  return shape;
};

// 初始化Three.js场景
const initThreeJs = () => {
  // 创建场景
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0xf8f4e8); // 浅米黄色背景，模拟宣纸

  // 创建相机
  camera = new THREE.PerspectiveCamera(
    60,
    window.innerWidth / window.innerHeight,
    0.1,
    3000
  );
  // 设置相机位置，从高处俯瞰
  camera.position.set(0, 250, 800);
  camera.lookAt(0, 100, 0);

  // 创建渲染器
  renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(window.devicePixelRatio);
  containerRef.value.appendChild(renderer.domElement);

  // 创建山水画元素
  createMountains();
  createWaterSurface();
  createClouds();
  createBirds();
  createSun();
  createTrees();
  initInkCanvas();

  // 开始动画循环
  animate();

  // 监听窗口大小变化
  window.addEventListener('resize', onWindowResize);

  // 监听鼠标事件
  window.addEventListener('mousemove', handleMouseMove);
  window.addEventListener('click', handleClick);

  // 取消自动墨滴效果，避免不必要的性能消耗
  // startAutoInkEffect();
};

// ===== 创建场景元素 =====
// 创建五层山峰（四层实体山峰 + 一层水面倒影）
const createMountains = () => {
  // 定义不同层次的山脉配置
  const mountainLayers = [
    { z: -500, width: 2500, height: 350, points: 25, irregularity: 0.3 }, // 最远层
    { z: -350, width: 2300, height: 280, points: 20, irregularity: 0.4 }, // 远层
    { z: -200, width: 2100, height: 200, points: 18, irregularity: 0.5 }, // 中层
    { z: 0, width: 1900, height: 120, points: 15, irregularity: 0.6 }     // 近层
  ];

  mountainLayers.forEach((layer, index) => {
    const path = createMountainPath(layer.width, layer.height, layer.points, layer.irregularity);
    const shape = createIrregularPolygon(path, -150); // 底部延伸到水面以下
    const geometry = new THREE.ShapeGeometry(shape);

    // 创建材质，应用模糊效果
    const material = new THREE.MeshBasicMaterial({
      color: visualParams.mountains[index].color,
      transparent: true,
      opacity: visualParams.mountains[index].opacity,
      side: THREE.DoubleSide
    });

    // 应用模糊滤镜
    if (visualParams.mountains[index].blur > 0) {
      // 在Three.js中实现模糊效果需要后处理，但为了简化，我们使用透明度和颜色来模拟
      material.opacity *= 0.7 + visualParams.mountains[index].blur * 0.1;
    }

    const mountain = new THREE.Mesh(geometry, material);
    mountain.position.z = layer.z;
    mountain.position.x = 0; // 居中

    scene.add(mountain);
    mountains.push(mountain);
  });
};

// 创建水面
const createWaterSurface = () => {
  const geometry = new THREE.PlaneGeometry(3000, 500);
  const material = new THREE.MeshBasicMaterial({
    color: 0x1a1a1a,
    transparent: true,
    opacity: 0.1
  });

  water = new THREE.Mesh(geometry, material);
  water.position.y = -150;
  water.rotation.x = -Math.PI / 2;
  water.position.z = 100;

  scene.add(water);
};

// 创建三层不同高度的云雾
const createClouds = () => {
  const cloudCountPerLayer = 2; // 每层减少为2朵云，但增加大小区分

  animationParams.clouds.heightLevels.forEach(height => {
    for (let i = 0; i < cloudCountPerLayer; i++) {
      // 使用更明确的大小区分，创建更常见的云朵形状
      const cloudSize = 200 + Math.random() * 300; // 增大云朵尺寸
      const geometry = new THREE.PlaneGeometry(cloudSize, cloudSize * 0.6, 8, 4);

      // 轻微扰动云的顶点，保持较为规则的云朵形状
      const vertices = geometry.attributes.position.array;
      for (let j = 0; j < vertices.length; j += 3) {
        // 减小扰动范围，保持云朵的自然外观
        vertices[j] += (Math.random() - 0.5) * 15;
        vertices[j + 2] += (Math.random() - 0.5) * 8;
      }
      geometry.attributes.position.needsUpdate = true;

      // 创建半透明材质，降低透明度使云雾更隐约
      const material = new THREE.MeshBasicMaterial({
        color: 0xf0f0f0, // 使用稍微偏灰的颜色而不是纯白
        transparent: true,
        opacity: 0.3 + Math.random() * 0.2, // 降低透明度至0.3-0.5
        side: THREE.DoubleSide,
        blending: THREE.AdditiveBlending
      });

      const cloud = new THREE.Mesh(geometry, material);
      cloud.rotation.y = Math.random() * Math.PI;
      cloud.position.x = -1000 + Math.random() * 2000;
      cloud.position.y = height;
      cloud.position.z = -400 + Math.random() * 300;

      scene.add(cloud);
      clouds.push({
        mesh: cloud,
        speed: animationParams.clouds.speed + (Math.random() - 0.5) * animationParams.clouds.variation,
        direction: -1, // 从右向左飘动
        height: height
      });
    }
  });
};

// 创建飞鸟群
const createBirds = () => {
  const { groupCount, birdsPerGroup, baseSpeed, speedVariation } = visualParams.birds;

  for (let group = 0; group < groupCount; group++) {
    const groupY = 150 + group * 70; // 调整高度分布
    const groupZ = -150 - group * 40;

    for (let i = 0; i < birdsPerGroup; i++) {
      // 创建更大的飞鸟形状，使其更容易识别
      const birdSize = 12; // 增大鸟的尺寸
      // 创建飞鸟形状（简化为两个三角形，但尺寸更大）
      const geometry = new THREE.BufferGeometry();
      const vertices = [
        -birdSize, 0, 0,
        birdSize, birdSize * 0.6, 0,
        0, 0, 0,
        -birdSize, 0, 0,
        birdSize, -birdSize * 0.6, 0,
        0, 0, 0
      ];

      geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));

      const material = new THREE.MeshBasicMaterial({
        color: 0x303030,
        transparent: true,
        opacity: 0.8
      });

      const bird = new THREE.Mesh(geometry, material);
      bird.position.x = 1000 + i * 40; // 增加间距
      bird.position.y = groupY + Math.sin(i) * 30;
      bird.position.z = groupZ + Math.random() * 20;

      scene.add(bird);
      birds.push({
        mesh: bird,
        speed: baseSpeed + (Math.random() - 0.5) * speedVariation,
        wingPhase: Math.random() * Math.PI * 2,
        group: group
      });
    }
  }
};

// 创建太阳
const createSun = () => {
  const geometry = new THREE.CircleGeometry(visualParams.sun.size, 32);
  const material = new THREE.MeshBasicMaterial({
    color: visualParams.sun.color,
    transparent: true,
    opacity: animationParams.sun.glowIntensity
  });

  sun = new THREE.Mesh(geometry, material);
  sun.position.set(visualParams.sun.x, visualParams.sun.y, visualParams.sun.z);

  // 添加光晕效果
  const glowGeometry = new THREE.CircleGeometry(visualParams.sun.size * 1.8, 32);
  const glowMaterial = new THREE.MeshBasicMaterial({
    color: 0xffb74d,
    transparent: true,
    opacity: 0.3,
    blending: THREE.AdditiveBlending
  });

  const glow = new THREE.Mesh(glowGeometry, glowMaterial);
  glow.position.copy(sun.position);

  scene.add(sun);
  scene.add(glow);
};

// 创建树木
const createTrees = () => {
  // 创建远景树木
  createTree(visualParams.trees.farTree);

  // 创建近景树木
  createTree(visualParams.trees.nearTree);
};

// 创建单棵树
const createTree = (treeConfig) => {
  const { x, y, z, size } = treeConfig;

  // 创建树干
  const trunkGeometry = new THREE.CylinderGeometry(3 * size, 2 * size, 30 * size, 8);
  const trunkMaterial = new THREE.MeshBasicMaterial({
    color: 0x5d4037,
    transparent: true,
    opacity: 0.8
  });

  const trunk = new THREE.Mesh(trunkGeometry, trunkMaterial);
  trunk.position.set(x, y + 15 * size, z);

  // 创建圆形树冠（传统国画风格）
  const crownGeometry = new THREE.CircleGeometry(20 * size, 16);
  crownGeometry.rotateX(-Math.PI / 2);
  const crownMaterial = new THREE.MeshBasicMaterial({
    color: 0x3a3a3a,
    transparent: true,
    opacity: 0.7
  });

  // 创建多层圆形树冠，形成传统国画风格的树形
  const crown1 = new THREE.Mesh(crownGeometry, crownMaterial);
  crown1.position.set(x, y + 40 * size, z);

  const crown2 = new THREE.Mesh(crownGeometry, crownMaterial);
  crown2.scale.set(0.8, 0.8, 0.8);
  crown2.position.set(x - 10 * size, y + 35 * size, z);

  const crown3 = new THREE.Mesh(crownGeometry, crownMaterial);
  crown3.scale.set(0.7, 0.7, 0.7);
  crown3.position.set(x + 10 * size, y + 38 * size, z);

  scene.add(trunk);
  scene.add(crown1);
  scene.add(crown2);
  scene.add(crown3);

  // 保存树引用
  if (z < -100) {
    farTree.value = { trunk, crown1, crown2, crown3 };
  } else {
    nearTree.value = { trunk, crown1, crown2, crown3 };
  }
};

// ===== 水墨交互效果 =====
// 初始化水墨Canvas
const initInkCanvas = () => {
  const canvas = inkCanvas.value;
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
  canvasContext = canvas.getContext('2d');
  canvasContext.globalCompositeOperation = 'multiply';
};

// 生成墨滴
const createInkDrop = (x, y, maxRadius, count = 1) => {
  for (let i = 0; i < count; i++) {
    const radius = Math.random() * maxRadius;
    const variationX = (Math.random() - 0.5) * 10;
    const variationY = (Math.random() - 0.5) * 10;

    inkParticles.push({
      x: x + variationX,
      y: y + variationY,
      radius: radius,
      maxRadius: radius * 3,
      opacity: 0.2 + Math.random() * 0.3,
      color: `rgba(0, 0, 0, ${0.2 + Math.random() * 0.3})`
    });
  }
};

// 更新墨滴动画
const updateInkParticles = () => {
  if (inkParticles.length === 0) return;

  // 清空canvas但保留透明度
  canvasContext.clearRect(0, 0, window.innerWidth, window.innerHeight);

  inkParticles = inkParticles.filter(particle => {
    // 更新墨滴
    particle.radius += animationParams.ink.spreadSpeed;
    particle.opacity -= animationParams.ink.fadeSpeed;

    // 绘制墨滴
    canvasContext.beginPath();
    canvasContext.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2);
    canvasContext.fillStyle = `rgba(0, 0, 0, ${particle.opacity})`;
    canvasContext.fill();

    // 绘制不规则形状，模拟水墨扩散效果
    for (let i = 0; i < 5; i++) {
      const angle = (Math.PI * 2 / 5) * i + time;
      const offsetX = Math.cos(angle) * particle.radius * 0.5;
      const offsetY = Math.sin(angle) * particle.radius * 0.5;

      canvasContext.beginPath();
      canvasContext.arc(
        particle.x + offsetX,
        particle.y + offsetY,
        particle.radius * 0.3,
        0,
        Math.PI * 2
      );
      canvasContext.fillStyle = `rgba(0, 0, 0, ${particle.opacity * 0.7})`;
      canvasContext.fill();
    }

    // 移除透明度为0的墨滴
    return particle.opacity > 0 && particle.radius < particle.maxRadius;
  });
};

// 处理鼠标移动
const handleMouseMove = (event) => {
  // 限制墨滴产生频率
  if (frameCount % interactionParams.mouse.inkFrequency === 0) {
    const deltaX = Math.abs(event.clientX - lastMouseX);
    const deltaY = Math.abs(event.clientY - lastMouseY);
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

    // 只有当鼠标移动足够距离时才产生墨滴
    if (distance > 5) {
      createInkDrop(
        event.clientX,
        event.clientY,
        interactionParams.mouse.maxInkRadius,
        1
      );
    }
  }

  lastMouseX = event.clientX;
  lastMouseY = event.clientY;
};

// 处理点击事件
const handleClick = (event) => {
  createInkDrop(
    event.clientX,
    event.clientY,
    interactionParams.click.maxInkRadius,
    interactionParams.click.inkCount
  );
};

// 启动自动墨滴效果（已禁用）
// const startAutoInkEffect = () => {
//   autoInkTimer = setInterval(() => {
//     const x = Math.random() * window.innerWidth;
//     const y = Math.random() * window.innerHeight * 0.8; // 主要在画面上部产生
//     createInkDrop(x, y, interactionParams.mouse.maxInkRadius * 0.8, 1);
//   }, interactionParams.autoInk.frequency);
// };

// ===== 动画循环 =====
const animate = () => {
  animationId = requestAnimationFrame(animate);
  time += 0.016;
  frameCount++;

  // 山脉动画：轻微移动，创造深远感
  mountains.forEach((mountain, index) => {
    const moveAmplitude = [5, 8, 10, 15][index]; // 近山移动幅度更大
    mountain.position.x = Math.sin(time * 0.1 + index) * moveAmplitude;

    // 轻微的高度变化，模拟山峰在云雾中若隐若现
    mountain.position.y = Math.sin(time * 0.05 + index) * 3;
  });

  // 水面波纹动画
  if (water) {
    water.position.y = -150 + Math.sin(time * animationParams.water.waveFrequency * 2) * animationParams.water.waveAmplitude;

    // 水面材质透明度随波纹变化
    water.material.opacity = 0.1 + Math.sin(time * animationParams.water.waveFrequency) * 0.02;
  }

  // 云雾动画：缓慢飘动
  clouds.forEach((cloud, index) => {
    cloud.mesh.position.x += cloud.speed * cloud.direction;

    // 添加垂直方向的飘动，模拟上升下降
    cloud.mesh.position.y = cloud.height + Math.sin(time * 0.2 + index) * 5;

    // 当云移出屏幕左侧时，从右侧重新进入
    if (cloud.mesh.position.x < -1500) {
      cloud.mesh.position.x = 1500;
    }
  });

  // 飞鸟动画：翅膀扇动和飞行
  birds.forEach((bird, index) => {
    // 向前飞行（从右向左）
    bird.mesh.position.x -= bird.speed;

    // 翅膀扇动效果
    const wingAngle = Math.sin(time * 5 + bird.wingPhase) * 0.3;
    bird.mesh.rotation.z = wingAngle;

    // 根据组设置不同的飞行高度变化，支持4组飞鸟
    const groupVariation = [0, 0.3, 0.6, 0.9][bird.group] || 0;
    bird.mesh.position.y += Math.sin(time * 0.3 + index) * (1 + groupVariation);

    // 当鸟飞出屏幕左侧时，从右侧重新进入
    if (bird.mesh.position.x < -1200) {
      bird.mesh.position.x = 1200;
      bird.mesh.position.y = 150 + bird.group * 70 + Math.random() * 50; // 调整高度范围
    }
  });

  // 太阳呼吸效果
  if (sun) {
    const breathingEffect = Math.sin(time * animationParams.sun.breathSpeed) * 0.2;
    sun.material.opacity = animationParams.sun.glowIntensity + breathingEffect;

    // 光晕缩放效果
    if (sun.parent.children.length > 1) {
      const glow = sun.parent.children.find(child => child !== sun);
      glow.scale.set(
        1 + breathingEffect * 0.3,
        1 + breathingEffect * 0.3,
        1
      );
    }
  }

  // 更新墨滴动画
  updateInkParticles();

  // 渲染场景
  renderer.render(scene, camera);
};

// ===== 事件处理 =====
const onWindowResize = () => {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);

  // 调整canvas大小
  if (inkCanvas.value) {
    inkCanvas.value.width = window.innerWidth;
    inkCanvas.value.height = window.innerHeight;
  }
};

// ===== 生命周期钩子 =====
onMounted(() => {
  if (containerRef.value && inkCanvas.value) {
    initThreeJs();
  }
});

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId);
  }

  window.removeEventListener('resize', onWindowResize);
  window.removeEventListener('mousemove', handleMouseMove);
  window.removeEventListener('click', handleClick);

  // autoInkTimer is no longer used as auto ink effect is disabled

  if (renderer && containerRef.value) {
    containerRef.value.removeChild(renderer.domElement);
    renderer.dispose();
  }
});
</script>

<style scoped>
.ink-background-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  pointer-events: none;
  overflow: hidden;
}

.ink-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}
</style>
