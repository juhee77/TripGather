const puppeteer = require('puppeteer');

(async () => {
    const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
    const page = await browser.newPage();
    
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });
    await page.type('input[name=\"email\"]', 'alex@test.com');
    await page.type('input[name=\"password\"]', 'pass1234');
    await page.click('button[type=\"submit\"]');
    await page.waitForNavigation({ waitUntil: 'networkidle0' });

    // Open create modal
    await page.click('button.primary-btn'); // Floating action button
    await new Promise(r => setTimeout(r, 500));

    // Fill form
    await page.type('input[name=\"title\"]', 'Test Gathering');
    await page.type('input[name=\"location\"]', 'Test Location');
    await page.type('input[name=\"date\"]', '0202-02-02');
    await page.type('input[name=\"time\"]', '12:00');
    // submit
    await page.click('button[type=\"submit\"]');
    
    await new Promise(r => setTimeout(r, 1000));
    
    // Go to 내 모임
    await page.evaluate(() => {
        const tabs = Array.from(document.querySelectorAll('.app-nav button'));
        const tab = tabs.find(t => t.textContent.includes('내 모임'));
        if (tab) tab.click();
    });
    
    await new Promise(r => setTimeout(r, 1000));
    await page.screenshot({ path: '/tmp/my_gatherings_test.png' });
    
    const count = await page.evaluate(() => {
        return document.querySelectorAll('.animate-fade').length;
    });
    console.log('Gatherings shown:', count);
    
    await browser.close();
})();
