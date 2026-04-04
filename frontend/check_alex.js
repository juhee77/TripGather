import puppeteer from 'puppeteer';

(async () => {
    const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 390, height: 844 });
    
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });
    await page.type('input[name=\"email\"]', 'alex@test.com');
    await page.type('input[name=\"password\"]', 'pass1234');
    await page.click('button[type=\"submit\"]');
    await page.waitForNavigation({ waitUntil: 'networkidle0' });
    
    await new Promise(r => setTimeout(r, 1000));
    await page.screenshot({ path: '/tmp/alex_home.png', fullPage: true });

    // Open Busan trip
    await page.evaluate(() => {
        const cards = Array.from(document.querySelectorAll('.animate-fade'));
        const busan = cards.find(c => c.textContent.includes('Busan'));
        if (busan) busan.click();
    });
    
    await new Promise(r => setTimeout(r, 1000));
    await page.screenshot({ path: '/tmp/alex_busan.png', fullPage: true });
    
    // Click Members tab
    await page.evaluate(() => {
        const tabs = Array.from(document.querySelectorAll('button'));
        const memTab = tabs.find(b => b.textContent === '멤버');
        if (memTab) memTab.click();
    });
    
    await new Promise(r => setTimeout(r, 1000));
    await page.screenshot({ path: '/tmp/alex_busan_members.png', fullPage: true });
    
    await browser.close();
})();
