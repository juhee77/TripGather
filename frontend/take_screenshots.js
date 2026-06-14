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
        headless: 'new',
        defaultViewport: { width: 390, height: 844 }, // Mobile viewport (iPhone 12/13 size)
        args: ['--no-sandbox'] 
    });
    const page = await browser.newPage();
    
    // Navigate to login
    console.log('Navigating to login page...');
    await page.goto(url + '/login', { waitUntil: 'networkidle0' });
    await new Promise(r => setTimeout(r, 1000));
    
    // Login
    console.log('Attempting to login with jihyun@test.com...');
    await page.type('input[name="email"]', 'jihyun@test.com');
    await page.type('input[name="password"]', 'pass1234');
    await page.click('button[type="submit"]');
    
    console.log('Waiting for navigation after login...');
    await page.waitForNavigation({ waitUntil: 'networkidle0' });
    await new Promise(r => setTimeout(r, 2000));

    // Hide scrollbars for cleaner screenshots
    await page.addStyleTag({ content: `
        ::-webkit-scrollbar { display: none; }
    `});

    // 1. Home Tabs Loop
    const homeTabs = ['라운지', '여행 피드', '내 여행', '내 여권'];
    for (const tabText of homeTabs) {
        console.log(`Navigating to Home and selecting tab: ${tabText}`);
        await page.goto(url + '/gather', { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 1500));
        
        await page.evaluate((text) => {
            const btns = Array.from(document.querySelectorAll('button'));
            const btn = btns.find(b => b.textContent.trim() === text);
            if (btn) {
                btn.click();
            } else {
                const elements = Array.from(document.querySelectorAll('a, span, div'));
                const el = elements.find(e => e.textContent.trim() === text);
                if (el) el.click();
            }
        }, tabText);

        await new Promise(r => setTimeout(r, 2500)); // wait for image load and render
        
        let filename = '';
        if (tabText === '라운지') filename = 'feed';
        if (tabText === '여행 피드') filename = 'flights';
        if (tabText === '내 여행') filename = 'mission';
        if (tabText === '내 여권') filename = 'passport';

        if (filename !== '') {
            console.log(`Capturing ${filename}.png...`);
            await page.screenshot({ path: path.join(outDir, `${filename}.png`) });
        }
    }

    // 2. Detail Page (Direct navigation to Gathering 1 detail)
    console.log('Capturing Detail Page (detail.png)...');
    try {
        await page.goto(url + '/gathering/1', { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 2500));
        await page.screenshot({ path: path.join(outDir, 'detail.png') });
    } catch (e) {
        console.log('Failed to capture detail.png', e);
    }

    // 3. My Gatherings / Host Management (Hosted & requests)
    console.log('Capturing hosted gatherings dashboard (my_gatherings.png)...');
    try {
        await page.goto(url + '/profile/hosting', { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 2000));
        await page.screenshot({ path: path.join(outDir, 'my_gatherings.png') });
    } catch (e) {
        console.log('Failed to capture my_gatherings.png', e);
    }

    // 4. Map Page
    console.log('Capturing Map (map.png)...');
    try {
        await page.goto(url + '/map', { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 4000)); // Wait for map to initialize and load points
        await page.screenshot({ path: path.join(outDir, 'map.png') });
    } catch (e) {
        console.log('Failed to capture map.png', e);
    }

    // 5. Chat Page (Entering the specific chat room with messages)
    console.log('Capturing Chat Room (chat.png)...');
    try {
        await page.goto(url + '/chat', { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 2000));
        
        // Click on the '강남 맛집 탐방' chat room
        await page.evaluate(() => {
            const chatCards = Array.from(document.querySelectorAll('.chat-item-card'));
            const targetCard = chatCards.find(c => c.textContent.includes('강남 맛집 탐방'));
            if (targetCard) {
                targetCard.click();
            } else if (chatCards.length > 0) {
                chatCards[0].click();
            }
        });
        
        await new Promise(r => setTimeout(r, 2500)); // Wait for chat log to load
        await page.screenshot({ path: path.join(outDir, 'chat.png') });
    } catch (e) {
        console.log('Failed to capture chat.png', e);
    }

    // 6. MyPage (profile.png)
    console.log('Capturing MyPage (profile.png)...');
    try {
        await page.goto(url + '/mypage', { waitUntil: 'networkidle0' });
        await new Promise(r => setTimeout(r, 2000));
        await page.screenshot({ path: path.join(outDir, 'profile.png') });
    } catch (e) {
        console.log('Failed to capture profile.png', e);
    }

    console.log('All screenshots regenerated successfully.');
    await browser.close();
})();
