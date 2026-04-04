const headers = { 'Content-Type': 'application/json' };
(async () => {
  const login = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST', headers,
    body: JSON.stringify({ email: 'alex@test.com', password: 'pass1234' })
  });
  const cookie = login.headers.get('set-cookie');
  
  const res = await fetch('http://localhost:8080/api/gatherings', {
    method: 'GET', headers: { 'Cookie': cookie }
  });
  const gatherings = await res.json();
  
  const currentUser = { name: "Alex", email: "alex@test.com" };
  const filtered = gatherings.filter(g => {
    const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
    const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === 'APPROVED' || m.status === 'PENDING'));
    return isHost || isApproved;
  });
  console.log("All Gatherings count:", gatherings.length);
  console.log("My Gatherings count:", filtered.length);
  console.log("My Gatherings titles:", filtered.map(g => g.title));
})();
