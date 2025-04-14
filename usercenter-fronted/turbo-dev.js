// turbo-dev.js - 用于启动带Turbo模式的Next.js开发服务器
const { exec } = require('child_process');

// 设置环境变量并启动Next.js
process.env.NEXT_TURBO = '1';
console.log('启动Turbo模式的Next.js开发服务器...');

// 使用子进程启动Next.js开发服务器
const devProcess = exec('next dev', { env: { ...process.env } });

// 将子进程的输出传递到主进程
devProcess.stdout.pipe(process.stdout);
devProcess.stderr.pipe(process.stderr);

// 处理主进程终止信号
process.on('SIGINT', () => {
  console.log('正在关闭开发服务器...');
  devProcess.kill('SIGINT');
  process.exit(0);
}); 