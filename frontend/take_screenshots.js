import puppeteer from 'puppeteer';
import fs from 'fs';
import path from 'path';

const url = 'http://localhost:5173';
const outDir = path.join(process.cwd(), '../docs/assets/screenshots');

if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

(async () => {
    console.log('Launching browser...');
    const browser = await puppeteer.launch({ 
        headless: 'new', // Use new headless mode
        defaultViewport: { width: 390, height: 844 }, // Mobile viewport (iPhone 12/13 size)
        args: ['--no-sandbox'] 
    });
    const page = await browser.newPage();
    
    // Navigate to local server
    console.log('Navigating to', url);
    await page.goto(url, { waitUntil: 'networkidle0' });

    console.log('Waiting for network requests to settle...');
    await new Promise(r => setTimeout(r, 2000));
    
    // Login to access tabs properly
    console.log('Attempting to login to access other tabs...');
    try {
        await page.goto(url + '/login', { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 1000));
        
        await page.type('input[name="email"]', 'jihyun@test.com');
        await page.type('input[name="password"]', 'pass1234');
        await page.click('button[type="submit"]');
        
        console.log('Waiting for navigate after login...');
        await page.waitForNavigation({ waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 2000));

        // Inject CSS to hide scrollbars and potentially overlapping right tabs/buttons
        await page.addStyleTag({ content: `
            ::-webkit-scrollbar { display: none; }
            .fixed.right-0, .fixed.right-4, .sticky { opacity: 0 !important; pointer-events: none !important; }
            nav.fixed.bottom-0 { opacity: 1 !important; pointer-events: all !important; } /* Keep bottom nav */
        `});
    } catch(e) {
        console.log('Login failed or not needed', e);
    }
    
    // 1. Feed
    console.log('Capturing Feed...');
    await page.goto(url, { waitUntil: 'networkidle0' });
    await new Promise(r => setTimeout(r, 2000));
    await page.screenshot({ path: path.join(outDir, 'feed.png') });

    // We will just do best effort screenshots of tabs on bottom nav:
    const tabs = ['발견', '일정', '나의 미션', '내 모임', '내 여권'];

    for (const tabText of tabs) {
        console.log(`Clicking tab: ${tabText}`);
        try {
            await page.evaluate((text) => {
                const btns = Array.from(document.querySelectorAll('button'));
                const btn = btns.find(b => b.textContent.trim() === text);
                if (btn) btn.click();
            }, tabText);
            
            await new Promise(r => setTimeout(r, 1000)); // wait for transition
            
            let filename = '';
            if (tabText === '일정') filename = 'flights';
            if (tabText === '나의 미션') filename = 'mission';
            if (tabText === '내 모임') filename = 'my_gatherings';
            if (tabText === '내 여권') filename = 'passport';
            
            if (filename !== '') {
                console.log(`Capturing ${filename}...`);
                await page.screenshot({ path: path.join(outDir, `${filename}.png`) });
            }
        } catch (e) {
            console.log(`Failed to screenshot tab ${tabText}`);
        }
    }

    // Try detail
    console.log('Capturing Detail Modal...');
    try {
        await page.goto(url, { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 1000));
        // Click the first feed card
        await page.evaluate(() => {
            const card = document.querySelector('.animate-fade');
            if (card) card.click();
        });
        await new Promise(r => setTimeout(r, 1000));
        await page.screenshot({ path: path.join(outDir, 'detail.png') });
    } catch (e) {
        console.log('Failed to screenshot detail.', e);
    }

    // Wait, the user mentioned gathering request accept/reject UI. We need to navigate to the host dashboard or view for "My Gatherings".
    console.log('Capturing map...');
    try {
        await page.goto(`${url}/map`, { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 2000));
        await page.screenshot({ path: path.join(outDir, 'map.png') });
    } catch (e) {
        console.log('Failed to screenshot map.', e);
    }

    console.log('Capturing chat...');
    try {
        await page.goto(`${url}/chat`, { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 2000));
        await page.screenshot({ path: path.join(outDir, 'chat.png') });
    } catch (e) {
        console.log('Failed to screenshot chat.', e);
    }
    
    console.log('Screenshots generated successfully.');
    await browser.close();
})();
