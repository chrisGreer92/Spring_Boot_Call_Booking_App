document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');

    const params = new URLSearchParams(location.search);
    const adminMode = params.get('admin') === 'true';

    const getURL = adminMode
        ? '/booking/admin?showPast=true&deleted=false&sort=startTime' //Admin gets specific (may tweak)
        : '/booking'; //Public gets the hardcoded return

    const statusColors = {
        available: '#007bff',
        pending: '#800080',
        rejected: '#ff0000',
        cancelled: '#000000'
    };

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'timeGridWeek',
        height: 'auto',
        allDaySlot: false,
        slotMinTime: "09:00:00",
        slotMaxTime: "19:00:00",
        firstDay: 1,
        hiddenDays: [0, 6], // Hides Sat & Sun
        buttonText: { today: 'This Week' },

        events: async (fetchInfo, successCallback, failureCallback) => {
            try {
                const res = await fetch(getURL);
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                const bookings = await res.json();

                const events = bookings.map(b => {
                    const statusLower = b.status.toLowerCase();
                    const statusFormatted = statusLower.charAt(0).toUpperCase() + statusLower.slice(1);

                    return {
                        id: b.id,
                        title: b.topic ? b.topic : statusFormatted,
                        start: b.startTime,
                        end: b.endTime,
                        color: statusColors[statusLower] || '#808080'
                    };
                });

                console.log('Loaded events:', events);
                successCallback(events);
            } catch (err) {
                console.error('Error fetching bookings', err);
                failureCallback(err);
            }
        },

        eventClick: function (info) {
            // Get relevent slot
            document.getElementById('slotId').value = info.event.id;
            document.querySelector('#bookingForm input[name="topic"]')
                .value = info.event.title !== 'Available' ? info.event.title : '';

            // Show Popover
            document.getElementById('bookingModal').style.display = 'flex';
        }
    });

    calendar.render();

    // Popover setup
    const bookingModal = document.getElementById('bookingModal');
    const closeModalBtn = document.getElementById('closeModal');
    const bookingForm = document.getElementById('bookingForm');

    // Close button
    closeModalBtn.addEventListener('click', function () {
        bookingModal.style.display = 'none';
        bookingForm.reset();
    });

    // Handle Request
    bookingForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const formData = Object.fromEntries(new FormData(bookingForm).entries());

        try {
            const res = await fetch(`/booking/${formData.slotId}/request`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: formData.name,
                    email: formData.email,
                    phone: formData.phone,
                    topic: formData.topic,
                    notes: formData.notes
                })
            });

            // Handle backend and potential errors
            if (!res.ok) {
                let errorMessage = `HTTP ${res.status}`;
                try {
                    const errorJson = await res.json();
                    const firstKey = Object.keys(errorJson)[0];
                    if (firstKey) {
                        errorMessage = errorJson[firstKey];
                    }
                } catch {
                    errorMessage = await res.text();
                }
                console.error('Booking failed:', errorMessage);
                alert(`Failed to book slot.\n${errorMessage}`);
                return;
            }

            // Success!
            alert('Booking Requested!');
            bookingModal.style.display = 'none';
            bookingForm.reset();
            calendar.refetchEvents();

        } catch (err) {
            // Handle fetch/network errors
            console.error('Booking request error:', err);
            alert(`Failed to book slot. ${err.message || err}`);
        }
    });
});
