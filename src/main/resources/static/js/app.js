const list = document.getElementById('exam-list');
const empty = document.getElementById('empty');
const error = document.getElementById('error');
const planError = document.getElementById('plan-error');
const examForm = document.getElementById('exam-form');
const subjectInput = document.getElementById('subject');
const dateInput = document.getElementById('date');
const timeInput = document.getElementById('time');
const submitBtn = document.getElementById('submit-btn');
const cancelEditBtn = document.getElementById('cancel-edit-btn');

// Tracks the ID of the exam currently being edited (null when creating a new exam)
let editingExamId = null;

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

// Load an exam into the form and switch submit mode to PUT
function startEditExam(exam) {
    error.textContent = '';
    editingExamId = exam.id;
    subjectInput.value = exam.subject;
    dateInput.value = exam.date;
    timeInput.value = exam.time;

    if (submitBtn) {
        submitBtn.textContent = 'Save changes';
    }
    if (cancelEditBtn) {
        cancelEditBtn.style.display = 'inline-block';
    }
    subjectInput.focus();
}

// Reset form back to create (POST) mode
function resetFormState() {
    editingExamId = null;
    examForm.reset();
    if (submitBtn) {
        submitBtn.textContent = 'Add exam';
    }
    if (cancelEditBtn) {
        cancelEditBtn.style.display = 'none';
    }
}

if (cancelEditBtn) {
    cancelEditBtn.addEventListener('click', () => {
        error.textContent = '';
        resetFormState();
    });
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

        const subjectName = document.createElement('div');
        subjectName.textContent = item.subject;
        subjectCell.appendChild(subjectName);

        if (item.examTime) {
            const examTime = document.createElement('small');
            examTime.className = 'exam-time';
            examTime.textContent = `Exam: ${item.examTime}`;
            subjectCell.appendChild(examTime);
        }

        if (item.postponedTo) {
            const postponedLabel = document.createElement('span');
            postponedLabel.className = 'postponed-label';
            postponedLabel.textContent = 'Postponed';
            subjectCell.appendChild(postponedLabel);
        }

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

        const blockDateInput = document.createElement('input');
        blockDateInput.type = 'date';
        blockDateInput.min = today;
        if (item.examDate) {
            blockDateInput.max = item.examDate;
        }
        blockDateInput.value = item.date >= today ? item.date : today;
        blockDateInput.setAttribute('aria-label', `New date for ${item.subject}`);

        const confirmBtn = document.createElement('button');
        confirmBtn.type = 'button';
        confirmBtn.textContent = 'Save';
        confirmBtn.style.marginLeft = '4px';

        postponeBtn.addEventListener('click', () => {
            const isHidden = pickerWrapper.style.display === 'none';
            pickerWrapper.style.display = isHidden ? 'inline-block' : 'none';
            if (isHidden) {
                blockDateInput.focus();
            }
        });

        confirmBtn.addEventListener('click', () => {
            postponeBlock(item.id, blockDateInput.value);
        });

        blockDateInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                postponeBlock(item.id, blockDateInput.value);
            }
        });

        pickerWrapper.appendChild(blockDateInput);
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

// Fetch and render the list of exams with Edit and Delete actions
async function loadExams() {
    const res = await fetch('/api/exams');
    const exams = await res.json();

    list.innerHTML = '';
    empty.style.display = exams.length ? 'none' : 'block';

    for (const e of exams) {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${e.subject}</td><td>${e.date}</td><td>${e.time}</td>`;

        const actionsCell = document.createElement('td');

        // Edit button loads exam data into the form
        const editBtn = document.createElement('button');
        editBtn.type = 'button';
        editBtn.textContent = 'Edit';
        editBtn.style.marginRight = '6px';
        editBtn.onclick = () => startEditExam(e);

        // Delete button removes the exam
        const delBtn = document.createElement('button');
        delBtn.type = 'button';
        delBtn.textContent = 'Delete';
        delBtn.onclick = () => deleteExam(e.id);

        actionsCell.appendChild(editBtn);
        actionsCell.appendChild(delBtn);
        tr.appendChild(actionsCell);
        list.appendChild(tr);
    }

    loadStudyPlan();
}

async function deleteExam(id) {
    if (editingExamId === id) {
        resetFormState();
    }
    await fetch('/api/exams/' + id, { method: 'DELETE' });
    loadExams();
    loadCountdown();
}

// Handle form submission: PUT when editing an existing exam, POST when adding a new one
examForm.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    error.textContent = '';

    const data = {
        subject: subjectInput.value,
        date: dateInput.value,
        time: timeInput.value
    };

    const isEditing = editingExamId !== null;
    const url = isEditing ? `/api/exams/${editingExamId}` : '/api/exams';
    const method = isEditing ? 'PUT' : 'POST';

    const res = await fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });

    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        error.textContent = body.error || 'Something went wrong';
        return;
    }

    resetFormState();
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