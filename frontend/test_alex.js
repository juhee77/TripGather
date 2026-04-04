import puppeteer from 'puppeteer';

(async () => {
    const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
    const page = await browser.newPage();
    
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });
    await page.type('input[name="email"]', 'alex@test.com');
    await page.type('input[name="password"]', 'pass1234');
    await page.click('button[type="submit"]');
    await page.waitForNavigation({ waitUntil: 'networkidle0' });
    
    // In Discover, click Weekend Trip to Busan
    await page.evaluate(() => {
        const cards = Array.from(document.querySelectorAll('.ui-card'));
        const busan = cards.find(c => c.textContent.includes('Busan'));
        if (busan) busan.click();
    });
    
    await new Promise(r => setTimeout(r, 1000));
    
    // Click Members tab
    await page.evaluate(() => {
        const tabs = Array.from(document.querySelectorAll('button'));
        const memTab = tabs.find(b => b.textContent === '멤버');
        if (memTab) memTab.click();
    });
    
    await new Promise(r => setTimeout(r, 1000));
    await page.screenshot({ path: '/tmp/alex_busan.png' });
    
    // Check if host section is visible
    const hostVisible = await page.evaluate(() => {
        const h3s = Array.from(document.querySelectorAll('h3'));
        return h3s.some(h => h.textContent.includes('참여 신청 관리'));
    });
    
    console.log('Is host section visible for Alex in Busan trip?', hostVisible);
    
    await browser.close();
})();
