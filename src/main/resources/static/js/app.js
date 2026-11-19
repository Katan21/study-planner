const list = document.getElementById('exam-list');
const empty = document.getElementById('empty');
const error = document.getElementById('error');
const planError = document.getElementById('plan-error');

// Local state for study blocks (supports both live API and mock fallback)
let currentBlocks = [];

const defaultMockBlocks = [
    {
        id: 1,
        date: new Date().toISOString().split('T')[0],
        subject: 'Math',
        task: 'Study — Math',
        status: 'TODO',
        examDate: new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0]
    },
    {
        id: 2,
        date: new Date(Date.now() + 86400000).toISOString().split('T')[0],
        subject: 'Math',
        task: 'Final revision',
        status: 'TODO',
        examDate: new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0]
    }
];

function showPlanError(message) {
    if (planError) {
        planError.textContent = message || '';
    }
}

// Mark a study block as DONE via POST /api/blocks/{id}/done
async function markBlockDone(blockId, checkbox) {
    showPlanError('');
    const targetBlock = currentBlocks.find(b => b.id === blockId);
    if (!targetBlock) return;

    try {
        const res = await fetch(`/api/blocks/${blockId}/done`, { method: 'POST' });

        // Fallback to local state when backend endpoint is not yet merged (404)
        if (res.status === 404) {
            targetBlock.status = checkbox.checked ? 'DONE' : 'TODO';
            renderStudyPlan(currentBlocks);
            return;
        }

        if (!res.ok) {
            const body = await res.json().catch(() => ({}));
            throw new Error(body.error || 'Failed to mark block as done.');
        }

        await loadStudyPlan();
    } catch (err) {
        if (err instanceof TypeError) {
            targetBlock.status = checkbox.checked ? 'DONE' : 'TODO';
            renderStudyPlan(currentBlocks);
            return;
        }
        checkbox.checked = targetBlock.status === 'DONE';
        showPlanError(err.message);
    }
}

// Postpone a study block via POST /api/blocks/{id}/postpone
async function postponeBlock(blockId, newDate) {
    showPlanError('');
    const targetBlock = currentBlocks.find(b => b.id === blockId);
    if (!targetBlock) return;

    if (!newDate) {
        showPlanError('Please select a target date.');
        return;
    }

    try {
        const res = await fetch(`/api/blocks/${blockId}/postpone?newDate=${encodeURIComponent(newDate)}`, {
            method: 'POST'
        });

        // Fallback validation when backend endpoint is not yet merged (404)
        if (res.status === 404) {
            const today = new Date().toISOString().split('T')[0];
            if (targetBlock.status === 'DONE') {
                showPlanError('Cannot postpone a done block.');
                return;
            }
            if (newDate < today) {
                showPlanError('Cannot postpone to a past date.');
                return;
            }
            if (targetBlock.examDate && newDate > targetBlock.examDate) {
                showPlanError('Cannot postpone past the exam date.');
                return;
            }
            targetBlock.date = newDate;
            renderStudyPlan(currentBlocks);
            return;
        }

        if (!res.ok) {
            const body = await res.json().catch(() => ({}));
            throw new Error(body.error || 'Failed to postpone block.');
        }

        await loadStudyPlan();
    } catch (err) {
        if (err instanceof TypeError) {
            targetBlock.date = newDate;
            renderStudyPlan(currentBlocks);
            return;
        }
        showPlanError(err.message);
    }
}

// Render study plan rows with Done checkbox and inline Postpone controls
function renderStudyPlan(blocks) {
    const emptyMessage = document.getElementById('empty-plan-message');
    const planTable = document.getElementById('plan-table');
    const tableBody = document.getElementById('plan-table-body');

    if (!emptyMessage || !planTable || !tableBody) return;

    tableBody.innerHTML = '';

    if (!blocks || blocks.length === 0) {
        emptyMessage.style.display = 'block';
        planTable.style.display = 'none';
        return;
    }

    emptyMessage.style.display = 'none';
    planTable.style.display = 'table';

    const today = new Date().toISOString().split('T')[0];

    blocks.forEach((item) => {
        const isDone = item.status === 'DONE';
        const row = document.createElement('tr');
        row.setAttribute('tabindex', '0');

        if (isDone) {
            row.style.opacity = '0.6';
            row.style.textDecoration = 'line-through';
        }

        // Done checkbox cell
        const doneCell = document.createElement('td');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.checked = isDone;
        checkbox.setAttribute('aria-label', `Mark ${item.subject} on ${item.date} as done`);
        checkbox.addEventListener('change', () => markBlockDone(item.id, checkbox));
        checkbox.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                checkbox.checked = !checkbox.checked;
                markBlockDone(item.id, checkbox);
            }
        });
        doneCell.appendChild(checkbox);

        // Date, Subject, Task cells
        const dateCell = document.createElement('td');
        dateCell.textContent = item.date;

        const subjectCell = document.createElement('td');
        subjectCell.textContent = item.subject;

        const taskCell = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = item.activity || item.task;
        taskCell.appendChild(strong);

        // Actions cell: Postpone button -> inline date picker + Save
        const actionsCell = document.createElement('td');
        const postponeBtn = document.createElement('button');
        postponeBtn.type = 'button';
        postponeBtn.textContent = 'Postpone';
        postponeBtn.disabled = isDone;

        const pickerWrapper = document.createElement('span');
        pickerWrapper.style.display = 'none';
        pickerWrapper.style.marginLeft = '8px';

        const dateInput = document.createElement('input');
        dateInput.type = 'date';
        dateInput.min = today;
        if (item.examDate) {
            dateInput.max = item.examDate;
        }
        dateInput.value = item.date >= today ? item.date : today;
        dateInput.setAttribute('aria-label', `New date for ${item.subject}`);

        const confirmBtn = document.createElement('button');
        confirmBtn.type = 'button';
        confirmBtn.textContent = 'Save';
        confirmBtn.style.marginLeft = '4px';

        postponeBtn.addEventListener('click', () => {
            const isHidden = pickerWrapper.style.display === 'none';
            pickerWrapper.style.display = isHidden ? 'inline-block' : 'none';
            if (isHidden) {
                dateInput.focus();
            }
        });

        confirmBtn.addEventListener('click', () => {
            postponeBlock(item.id, dateInput.value);
        });

        dateInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                postponeBlock(item.id, dateInput.value);
            }
        });

        pickerWrapper.appendChild(dateInput);
        pickerWrapper.appendChild(confirmBtn);
        actionsCell.appendChild(postponeBtn);
        actionsCell.appendChild(pickerWrapper);

        row.appendChild(doneCell);
        row.appendChild(dateCell);
        row.appendChild(subjectCell);
        row.appendChild(taskCell);
        row.appendChild(actionsCell);
        tableBody.appendChild(row);
    });
}

// Fetch and render the study plan starting from today's date
async function loadStudyPlan() {
    const today = new Date().toISOString().split('T')[0];
    showPlanError('');

    try {
        const response = await fetch('/api/plan?startDate=' + today);
        if (!response.ok) {
            throw new Error('Failed to fetch study plan');
        }
        const data = await response.json();

        if (data && data.length > 0) {
            // Ensure each item has an id, status, and examDate for UI actions
            currentBlocks = data.map((item, idx) => ({
                ...item,
                id: item.id || (idx + 1),
                status: item.status || 'TODO',
                examDate: item.examDate || data.filter(d => d.subject === item.subject).map(d => d.date).sort().pop()
            }));
        } else {
            currentBlocks = defaultMockBlocks.map(b => ({ ...b }));
        }
        renderStudyPlan(currentBlocks);
    } catch (err) {
        console.warn('Using mock study plan data:', err.message);
        if (currentBlocks.length === 0) {
            currentBlocks = defaultMockBlocks.map(b => ({ ...b }));
        }
        renderStudyPlan(currentBlocks);
    }
}

// Fetch and render the list of exams
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

    loadStudyPlan();
}

async function deleteExam(id) {
    await fetch('/api/exams/' + id, { method: 'DELETE' });
    loadExams();
    loadCountdown();
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
    loadCountdown();
});

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

loadExams();
loadCountdown();