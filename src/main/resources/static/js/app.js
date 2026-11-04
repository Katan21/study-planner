const list = document.getElementById('exam-list');
const empty = document.getElementById('empty');
const error = document.getElementById('error');

async function loadExams() {
    const res = await fetch('/api/exams');
    const exams = await res.json();

    list.innerHTML = '';
    empty.style.display = exams.length ? 'none' : 'block';

    for (const e of exams) {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${e.subject}</td><td>${e.date}</td><td>${e.time}</td>`;

        const del = document.createElement('td');
        const btn = document.createElement('button');

        btn.textContent = 'Delete';
        btn.onclick = () => deleteExam(e.id);

        del.appendChild(btn);
        tr.appendChild(del);
        list.appendChild(tr);
    }
}

async function deleteExam(id) {
    await fetch('/api/exams/' + id, { method: 'DELETE' });
    loadExams();
}

document.getElementById('exam-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    error.textContent = '';

    const data = {
        subject: document.getElementById('subject').value,
        date: document.getElementById('date').value,
        time: document.getElementById('time').value
    };

    const res = await fetch('/api/exams', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });

    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        error.textContent = body.error || 'Something went wrong';
        return;
    }

    ev.target.reset();
    loadExams();
});

loadExams();
async function loadCountdown() {
  const el = document.getElementById('countdown');
  if (!el) return;
  try {
    const res = await fetch('/api/countdown');
    const data = await res.json();
    if (data.daysLeft < 0) { el.textContent = ''; return; }
    el.textContent = data.warning
      ? '⚠ Exam in ' + data.daysLeft + ' day(s) — start now!'
      : 'Nearest exam in ' + data.daysLeft + ' day(s)';
    el.classList.toggle('warning', !!data.warning);
  } catch (e) { el.textContent = ''; }
}
loadCountdown();
