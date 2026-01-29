// Get server-side data
const { busVoyageDate, classePlaces, availableSeatsByType } = window.RESERVATION_CONFIG;

const pricingMap = {};
            
let selectedSeatsByType = {};
Object.keys(availableSeatsByType).forEach(type => {
    selectedSeatsByType[type] = 0;
});

const seatCheckboxes = document.querySelectorAll('.seat-checkbox');
const selectedSeatsInfo = document.getElementById('selectedSeatsInfo');
const selectedSeatsList = document.getElementById('selectedSeatsList');
const selectedSeatsDetails = document.getElementById('selectedSeatsDetails');
const seatClassSelectors = document.getElementById('seatClassSelectors');
const totalAmountSpan = document.getElementById('totalAmount');
const discountInfoDiv = document.getElementById('discountInfo');
const discountMessage = document.getElementById('discountMessage');
const submitBtn = document.getElementById('submitBtn');
const clientSelect = document.getElementById('clientSelect');
const dateReservationInput = document.getElementById('dateReservation');
const inputNumberPlace = document.getElementById('inputNumberPlace');
const bulkClasseSelect = document.getElementById('bulkClasseSelect');
const multiplePaymentToggle = document.getElementById('multiplePaymentToggle');
const singlePayment = document.getElementById('singlePayment');
const multiplePayment = document.getElementById('multiplePayment');

// Store client age group info
const clientAgeGroupMap = {};
document.querySelectorAll('#clientSelect option').forEach(option => {
    if (option.value) {
        clientAgeGroupMap[option.value] = {
            ageGroupId: option.dataset.ageGroupId ? parseInt(option.dataset.ageGroupId) : null,
            ageGroupLabel: option.dataset.ageGroupLabel
        };
    }
});

//*-- Set default date to today
const today = new Date().toISOString().split('T')[0];
const dateToUse = today <= busVoyageDate ? today : busVoyageDate;
dateReservationInput.value = dateToUse;
dateReservationInput.max = busVoyageDate;

//?-- Fetch dynamic pricing when client or date changes
async function fetchPricing(ageGroupId, date) {
    if (!ageGroupId || !date) return;
    // Prefer voyage-aware pricing when possible
    const voyageId = window.RESERVATION_CONFIG?.voyageId;
    try {
        if (voyageId) {
            const response = await fetch(`/api/pricing/voyage/${voyageId}?date=${date}`);
            if (!response.ok) throw new Error('Failed to fetch voyage pricing');
            const data = await response.json();
            // data shape: {classePlaceId: { ageGroupId: {price, hasDiscount, percentage, ...}, ...}, ...}
            const mapForAge = {};
            for (const [classePlaceId, ageGroupMap] of Object.entries(data)) {
                const ageInfo = ageGroupMap[ageGroupId];
                if (ageInfo) {
                    mapForAge[classePlaceId] = {
                        price: ageInfo.price,
                        hasDiscount: !!ageInfo.hasDiscount,
                        percentage: ageInfo.percentage || 0
                    };
                }
            }
            // Ensure we have entries for all classePlaces (fallback to base price)
            window.RESERVATION_CONFIG.classePlaces.forEach(cp => {
                if (!mapForAge[cp.id]) {
                    mapForAge[cp.id] = {price: cp.prixPlace || 0, hasDiscount: false, percentage: 0};
                }
            });
            pricingMap[ageGroupId] = mapForAge;
            return;
        }

        // Legacy fallback: request non-voyage pricing
        const response = await fetch(`/api/pricing?ageGroupId=${ageGroupId}&date=${date}`);
        const data = await response.json();
        pricingMap[ageGroupId] = data; // {classePlaceId: {price, hasDiscount, percentage}}
    } catch (error) {
        console.error('Error fetching pricing:', error);
    }
}

//?-- Get price for a seat class based on age group (looks in ClasseAgeConf)
function getEffectivePrice(ageGroupId, classePlaceId) {
    if (pricingMap[ageGroupId] && pricingMap[ageGroupId][classePlaceId]) {
        return pricingMap[ageGroupId][classePlaceId];
    }
    
    // Fallback to base price
    const cp = classePlaces.find(c => c.id == classePlaceId);
    return {price: cp?.prixPlace || 0, hasDiscount: false, percentage: 0};
}

function countSeatClasses() {
    // Reset counts
    Object.keys(selectedSeatsByType).forEach(type => {
        selectedSeatsByType[type] = 0;
    });
    
    const selects = document.querySelectorAll('.seat-class-select');
    selects.forEach(select => {
        const selectedOption = classePlaces.find(cp => cp.id == select.value);
        if (selectedOption) {
            selectedSeatsByType[selectedOption.libelle] = 
                (selectedSeatsByType[selectedOption.libelle] || 0) + 1;
        }
    });
    
    // Update badges dynamically
    Object.keys(availableSeatsByType).forEach(type => {
        const badge = document.getElementById('available' + type + 'Badge');
        if (badge) {
            const available = availableSeatsByType[type];
            const selected = selectedSeatsByType[type] || 0;
            badge.textContent = (available - selected);
        }
    });
}

function getCurrentAgeGroupId() {
    const clientId = clientSelect.value;
    return clientAgeGroupMap[clientId]?.ageGroupId || null;
}

async function updateSelectedSeats() {
    const selected = Array.from(seatCheckboxes)
        .filter(cb => cb.checked && !cb.disabled)
        .map(cb => cb.value);
    
    if (selected.length > 0) {
        selectedSeatsInfo.style.display = 'block';
        selectedSeatsDetails.style.display = 'block';
        selectedSeatsList.textContent = selected.join(', ');
        
        const ageGroupId = getCurrentAgeGroupId();
        const date = dateReservationInput.value;
        
        // Fetch pricing if needed
        if (ageGroupId && date && !pricingMap[ageGroupId]) {
            await fetchPricing(ageGroupId, date);
        }
        
        seatClassSelectors.innerHTML = '';
        selected.forEach(seatNum => {
            const div = document.createElement('div');
            div.className = 'row mb-2 align-items-center seat-class-row';
            div.dataset.seat = seatNum;
            
            let optionsHtml = '<option value="">-- Choisir classe --</option>';
            classePlaces.forEach(cp => {
                const priceInfo = getEffectivePrice(ageGroupId, cp.id);
                const displayText = priceInfo.hasDiscount ? 
                    `${cp.libelle} - ${priceInfo.price.toLocaleString('fr-FR')} Ar (-${priceInfo.percentage.toFixed(0)}%)` :
                    `${cp.libelle} - ${priceInfo.price.toLocaleString('fr-FR')} Ar`;
                
                optionsHtml += `<option value="${cp.id}" 
                    data-prix="${priceInfo.price}" 
                    ${priceInfo.hasDiscount ? 'data-discounted="true"' : ''}>
                    ${displayText}
                </option>`;
            });
            
            div.innerHTML = `
                <div class="col-auto">
                    <span class="badge bg-primary">Place ${seatNum}</span>
                </div>
                <div class="col">
                    <select name="seatClasses" class="form-select seat-class-select" required data-seat="${seatNum}">
                        ${optionsHtml}
                    </select>
                </div>
                <div class="col-auto">
                    <span class="price-display text-success fw-bold"></span>
                </div>
            `;
            seatClassSelectors.appendChild(div);
        });

        // Add event listeners to new selects
        document.querySelectorAll('.seat-class-select').forEach(select => {
            select.addEventListener('change', function() {
                const selectedOption = this.options[this.selectedIndex];
                const prix = selectedOption.dataset.prix;
                const isDiscounted = selectedOption.hasAttribute('data-discounted');
                
                // Update price display next to select
                const priceDisplay = this.closest('.seat-class-row').querySelector('.price-display');
                if (prix) {
                    priceDisplay.textContent = parseFloat(prix).toLocaleString('fr-FR') + ' Ar';
                    if (isDiscounted) {
                        priceDisplay.classList.add('text-warning');
                    } else {
                        priceDisplay.classList.remove('text-warning');
                    }
                } else {
                    priceDisplay.textContent = '';
                }
                
                calculateTotal(); // Only call calculateTotal here
            });
        });
        
        calculateTotal();
        submitBtn.disabled = false;
    } else {
        selectedSeatsInfo.style.display = 'none';
        selectedSeatsDetails.style.display = 'none';
        seatClassSelectors.innerHTML = '';
        submitBtn.disabled = true;
        discountInfoDiv.style.display = 'none';
    }
    
    countSeatClasses();
    updateRemainingAmount(); // Safe to call here (no recursion)

    // Keep the numeric input in sync
    if (inputNumberPlace) {
        inputNumberPlace.value = selected.length > 0 ? selected.length : '';
    }
}

function calculateTotal() {
    let total = 0;
    let hasDiscountedSeats = false;
    const selects = document.querySelectorAll('.seat-class-select');
    
    selects.forEach(select => {
        const selectedOption = select.options[select.selectedIndex];
        if (selectedOption && selectedOption.dataset.prix) {
            total += parseFloat(selectedOption.dataset.prix);
            if (selectedOption.hasAttribute('data-discounted')) {
                hasDiscountedSeats = true;
            }
        }
    });
    
    totalAmountSpan.textContent = total.toLocaleString('fr-FR', {minimumFractionDigits: 0, maximumFractionDigits: 0});
    
    // Show discount info if applicable
    if (hasDiscountedSeats) {
        discountInfoDiv.style.display = 'block';
    } else {
        discountInfoDiv.style.display = 'none';
    }
    
    countSeatClasses();
    updateRemainingAmount();
    
    return total;
}

seatCheckboxes.forEach(cb => {
    cb.addEventListener('change', updateSelectedSeats);
});

// Apply number-based quick selection: check first N available seats
if (inputNumberPlace) {
    inputNumberPlace.addEventListener('input', function() {
        const v = parseInt(this.value) || 0;
        const max = parseInt(this.max) || Infinity;
        const toSelect = Math.max(0, Math.min(v, max));

        // build list of available checkboxes (not disabled)
        const available = Array.from(seatCheckboxes).filter(cb => !cb.disabled);

        // uncheck all
        available.forEach(cb => cb.checked = false);

        // check first toSelect items
        for (let i = 0; i < toSelect && i < available.length; i++) {
            available[i].checked = true;
        }

        updateSelectedSeats();
    });
}

// Bulk class apply: set all seat-class-select values and trigger change
if (bulkClasseSelect) {
    bulkClasseSelect.addEventListener('change', function() {
        const val = this.value;
        if (!val) return;
        const selects = document.querySelectorAll('.seat-class-select');
        selects.forEach(s => {
            s.value = val;
            s.dispatchEvent(new Event('change', {bubbles: true}));
        });
        calculateTotal();
    });
}

// Update prices when client changes
clientSelect.addEventListener('change', function() {
    pricingMap[getCurrentAgeGroupId()] = null; // Clear cache
    if (document.querySelectorAll('.seat-class-select').length > 0) {
        updateSelectedSeats();
    }
});

dateReservationInput.addEventListener('change', function() {
    pricingMap[getCurrentAgeGroupId()] = null; // Clear cache
    if (document.querySelectorAll('.seat-class-select').length > 0) {
        updateSelectedSeats();
    }
});

multiplePaymentToggle.addEventListener('change', function() {
    if (this.checked) {
        singlePayment.style.display = 'none';
        multiplePayment.style.display = 'block';
        document.querySelector('.single-caisse').disabled = true;
        document.querySelector('.single-caisse').removeAttribute('required');
        document.querySelectorAll('.payment-row select').forEach(s => s.disabled = false);
        document.querySelectorAll('.payment-row input').forEach(i => i.disabled = false);
    } else {
        singlePayment.style.display = 'block';
        multiplePayment.style.display = 'none';
        document.querySelector('.single-caisse').disabled = false;
        document.querySelector('.single-caisse').setAttribute('required', '');
        document.querySelectorAll('.payment-row select').forEach(s => s.disabled = true);
        document.querySelectorAll('.payment-row input').forEach(i => i.disabled = true);
    }
});

document.getElementById('addPaymentMethod').addEventListener('click', function() {
    const paymentMethods = document.getElementById('paymentMethods');
    const newRow = paymentMethods.querySelector('.payment-row').cloneNode(true);
    newRow.querySelectorAll('input').forEach(input => input.value = '');
    newRow.querySelectorAll('select').forEach(select => select.value = '');
    paymentMethods.appendChild(newRow);
    updateRemainingAmount();
});

document.getElementById('paymentMethods').addEventListener('input', function(e) {
    if (e.target.classList.contains('montant-input')) {
        const totalPrice = parseFloat(totalAmountSpan.textContent.replace(/\s/g, '')) || 0;
        let value = parseFloat(e.target.value) || 0;
        
        if (value < 0) {
            value = 0;
            e.target.value = 0;
        }
        
        const montantInputs = Array.from(document.querySelectorAll('.montant-input'));
        const currentTotal = montantInputs.reduce((sum, input) => {
            if (input === e.target) return sum;
            return sum + (parseFloat(input.value) || 0);
        }, 0);
        
        const maxAllowed = totalPrice - currentTotal;
        
        if (value > maxAllowed) {
            e.target.value = Math.max(0, maxAllowed).toFixed(2);
        }
    }
    updateRemainingAmount();
});

function updateRemainingAmount() {
    if (!multiplePaymentToggle.checked) return;
    
    const totalPrice = parseFloat(totalAmountSpan.textContent.replace(/\s/g, '')) || 0;
    const montantInputs = Array.from(document.querySelectorAll('.montant-input'));
    const totalPaid = montantInputs.reduce((sum, input) => {
        return sum + (parseFloat(input.value) || 0);
    }, 0);
    
    const remaining = totalPrice - totalPaid;
    const remainingDiv = document.getElementById('remainingAmount');
    
    if (Math.abs(remaining) > 0.01) {
        remainingDiv.style.display = 'block';
        
        if (remaining > 0) {
            remainingDiv.className = 'mt-3 alert alert-light-warning';
            remainingDiv.innerHTML = '<i class="bi bi-exclamation-triangle me-2"></i><strong>Reste à payer:</strong> ' + 
                remaining.toLocaleString('fr-FR', {minimumFractionDigits: 0, maximumFractionDigits: 0}) + ' Ar';
        } else {
            remainingDiv.className = 'mt-3 alert alert-light-success';
            remainingDiv.innerHTML = '<i class="bi bi-check-circle me-2"></i><strong>✓ Paiement complet!</strong>';
        }
    } else {
        remainingDiv.style.display = 'none';
    }
}

document.getElementById('reservationForm').addEventListener('submit', function(e) {
    // Validate seat classes are selected
    const selects = document.querySelectorAll('.seat-class-select');
    let allSelected = true;
    selects.forEach(select => {
        if (!select.value) {
            allSelected = false;
        }
    });
    
    if (!allSelected) {
        e.preventDefault();
        alert('Veuillez sélectionner une classe pour chaque place');
        return false;
    }
    
    // Validate seat type availability
    Object.keys(availableSeatsByType).forEach(type => {
        const available = availableSeatsByType[type];
        const selected = selectedSeatsByType[type] || 0;
        if (selected > available) {
            e.preventDefault();
            alert(`Trop de places ${type} sélectionnées (${selected}/${available})`);
            return false;
        }
    });
    
    if (multiplePaymentToggle.checked) {
        const totalPrice = parseFloat(totalAmountSpan.textContent.replace(/\s/g, '')) || 0;
        const montantInputs = Array.from(document.querySelectorAll('.montant-input'));
        const totalPaid = montantInputs.reduce((sum, input) => sum + (parseFloat(input.value) || 0), 0);
        
        if (Math.abs(totalPaid - totalPrice) > 0.01) {
            e.preventDefault();
            alert('Le montant total des paiements doit être égal au prix total: ' + totalPrice.toLocaleString('fr-FR') + ' Ar');
            return false;
        }
    }
});

// Handle client creation
document.getElementById('createClientForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const formData = new FormData(this);
    const data = Object.fromEntries(formData);
    
    try {
        const response = await fetch('/client/create', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            const client = await response.json();
            const option = new Option(client.nom + ' ' + client.prenom, client.id, true, true);
            // Add age category info as data attributes
            if (client.categorieGroupeAge) {
                option.dataset.ageGroupId = client.categorieGroupeAge.id;
                option.dataset.ageGroupLabel = client.categorieGroupeAge.libelle;
            }
            document.getElementById('clientSelect').add(option);
            clientAgeGroupMap[client.id] = {
                ageGroupId: client.categorieGroupeAge?.id,
                ageGroupLabel: client.categorieGroupeAge?.libelle
            };
            bootstrap.Modal.getInstance(document.getElementById('createClientModal')).hide();
            this.reset();
        } else {
            alert('Erreur lors de la création du client');
        }
    } catch (error) {
        alert('Erreur: ' + error.message);
    }
});