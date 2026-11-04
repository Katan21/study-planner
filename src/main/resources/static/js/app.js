const list = document.getElementById('exam-list');
const empty = document.getElementById('empty');
const error = document.getElementById('error');

async function loadExams() {
	
	// Fetch and render the study plan for the current date
	function loadStudyPlan() {
	    const today = new Date().toISOString().split('T')[0];

	    fetch('/api/plan?startDate=' + today)
	        .then(response => {
	            if (!response.ok) {
	                throw new Error('Failed to fetch plan');
	            }
	            return response.json();
	        })
	        .then(data => {
	            const emptyMessage = document.getElementById('empty-plan-message');
	            const planTable = document.getElementById('plan-table');
	            const tableBody = document.getElementById('plan-table-body');

	            tableBody.innerHTML = '';

	            if (!data || data.length === 0) {
	                emptyMessage.style.display = 'block';
	                planTable.style.display = 'none';
	                return;
	            }

	            emptyMessage.style.display = 'none';
	            planTable.style.display = 'table';

	            data.forEach(item => {
	                const row = document.createElement('tr');
	                row.setAttribute('tabindex', '0');
	                row.innerHTML = `
	                    <td>${item.date}</td>
	                    <td>${item.subject}</td>
	                    <td><strong>${item.activity}</strong></td>
	                `;
	                tableBody.appendChild(row);
	            });
	        })
	        .catch(error => console.error('Error loading study plan:', error));
	}
	
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
