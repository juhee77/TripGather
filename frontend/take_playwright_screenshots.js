import { chromium } from 'playwright';
import path from 'path';
import fs from 'fs';

const url = 'http://localhost:5173';
const outDir = path.join(process.cwd(), '../docs/assets/screenshots');

if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

(async () => {
  console.log('Launching browser via Playwright...');
  const browser = await chromium.launch({
    headless: true,
  });
  
  // Create context with mobile viewport (iPhone 12/13 size)
  const context = await browser.newContext({
    viewport: { width: 390, height: 844 },
    deviceScaleFactor: 2, // High DPI for crystal-clear screenshots
  });
  
  const page = await context.newPage();

  // 1. Capture Login Page
  console.log('Capturing Login Page...');
  await page.goto(`${url}/login`);
  await page.waitForTimeout(1000);
  await page.screenshot({ path: path.join(outDir, 'login.png') });

  // Login
  console.log('Logging in...');
  await page.fill('input[name="email"]', 'jihyun@test.com');
  await page.fill('input[name="password"]', 'pass1234');
  await page.click('button[type="submit"]');

  // Wait for main feed page to load
  console.log('Waiting for Main Feed...');
  await page.waitForURL(`${url}/gather`);
  await page.waitForTimeout(2000);
  
  // Inject CSS to hide scrollbars for cleaner screenshots
  await page.addStyleTag({ content: `
    ::-webkit-scrollbar { display: none !important; }
    * { -ms-overflow-style: none !important; scrollbar-width: none !important; }
  `});

  // 2. Feed Page
  console.log('Capturing Feed...');
  await page.screenshot({ path: path.join(outDir, 'feed.png') });

  // 3. Detail Page
  console.log('Capturing Detail page...');
  await page.goto(`${url}/gathering/1`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, 'detail.png') });

  // 4. Create Gathering Page
  console.log('Capturing Create Gathering Page...');
  await page.goto(`${url}/create`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, 'create_gathering.png') });

  // 5. Itinerary Editor (Night Flight)
  console.log('Capturing Itinerary Editor...');
  await page.goto(`${url}/itinerary/create`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, 'itinerary_editor.png') });

  // 6. Trip Hub / Boarding Pass
  console.log('Capturing Trip Hub...');
  await page.goto(`${url}/trip/1`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, 'trip_hub.png') });

  // 7. Chat page
  console.log('Capturing Chat page...');
  await page.goto(`${url}/chat`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, 'chat.png') });

  // 8. Passport / MyPage
  console.log('Capturing Passport...');
  await page.goto(`${url}/mypage`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, 'passport.png') });

  // 9. Host Dashboard
  console.log('Capturing Host Dashboard...');
  await page.goto(`${url}/profile/hosting`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, 'host_dashboard.png') });

  // 10. Map Page
  console.log('Capturing Map...');
  await page.goto(`${url}/map`);
  await page.waitForTimeout(2500); // Give it a bit more time for the map to render
  await page.screenshot({ path: path.join(outDir, 'map.png') });

  console.log('All screenshots captured successfully!');
  await browser.close();
})();
