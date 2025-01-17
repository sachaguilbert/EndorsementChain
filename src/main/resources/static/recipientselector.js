/*
 * Functionality to generate the HTML and JS required to select a transferee (possibly across platforms)
 * Used in issuebl and twice in transportdocumenttransfer (for title and possession)
 * Generates :
 *  * a dropdown to select the target platform
 *  * a text input to type in the selected platform (removed)
 *  * a text input to allow fetching the transferee's information from the target platform
 *  * a button to activate the fetching
 *  * a dropdown to select the transferee from the local address book
 */

var recipientSelector = async function(recipientSelectorDiv, transfereePublicKeyTextArea, testPlatforms) {

    const receivingRegistryHtmlId = recipientSelectorDiv + "ReceivingRegistry";
    const receivingRegistrySelectHtmlId = recipientSelectorDiv + "ReceivingRegistrySelect";
    const addressBookSelectHtmlId = recipientSelectorDiv + "AddressBookSelect";
    const recipientIdHtmlId = recipientSelectorDiv + "RecipientId";
    const fetchRecipientCheckmark = recipientSelectorDiv + "Checkmark";
    document.getElementById(recipientSelectorDiv).innerHTML = `
        <div class="form-check" style="padding-left:0;">
            <label class="form-label" style="position:relative;padding-left:0;">Select recipient from receiving eBL Platform</label>
            <div style="display:flex;">
                <select id="${receivingRegistrySelectHtmlId}" class="form-select form-control">
                    <option value="" selected>Select...</option>
                </select>
                <input type="text" class="form-control" id="${receivingRegistryHtmlId}" style="display:none;"><br>
                <input type="text" class="form-control" id="${recipientIdHtmlId}" placeholder="Enter identifier">
                <div id="${fetchRecipientCheckmark}"></div>
            </div>
        </div>
        <div class="form-check" style="padding-left:0;">
            <label class="form-label" for="textAreaExample" style="position:relative;padding-left:0;">Or select from local address book</label>
            <select id="${addressBookSelectHtmlId}" class="form-select form-control">
                <option value='{"publicKey": "", "eblPlatform": ""}' selected>Select...</option>
            </select>
        </div>`;

    var xhrAddressBook = new XMLHttpRequest();
    xhrAddressBook.open("GET", "/api/v1/parties/");
    xhrAddressBook.onload = function() {
        const addressBookEntries = JSON.parse(xhrAddressBook.response);
        let selectTransferTitle = document.getElementById(addressBookSelectHtmlId);
        addressBookEntries.forEach(function(entry){
            let newOption = document.createElement("option");
            newOption.value = JSON.stringify({publicKey: entry.publicKey, eblPlatform: entry.eblPlatform});
            newOption.innerText = entry.name + " (" + entry.thumbprint + ")" + (entry.eblPlatform == ""?  " (local)" : " on " + entry.eblPlatform);
            selectTransferTitle.appendChild(newOption);
            document.getElementById(addressBookSelectHtmlId).addEventListener("change", function (event){
                document.getElementById(transfereePublicKeyTextArea).value = JSON.parse(event.target.value)["publicKey"];
                document.getElementById(receivingRegistryHtmlId).value = JSON.parse(event.target.value)["eblPlatform"];
            });
        });
    }
    xhrAddressBook.send();

    testPlatforms.forEach(function(platform) {
        let newOption = document.createElement("option");
        newOption.value = JSON.stringify(platform);
        newOption.innerHTML= platform.name;
        let transferPossessionPlatformSelect = document.getElementById(receivingRegistrySelectHtmlId);
        transferPossessionPlatformSelect.appendChild(newOption);
        transferPossessionPlatformSelect.addEventListener("change", function (event){
            document.getElementById(receivingRegistryHtmlId).value  = event.target.value == ""? "" : JSON.parse(event.target.value)["url"];
        });
    });

    let fetchRecipientInput = document.getElementById(recipientIdHtmlId);
    fetchRecipientInput.onchange= async _ => {
        getAndCheckRecipient();
    }

    let fetchRecipientSelect = document.getElementById(receivingRegistrySelectHtmlId);
    fetchRecipientSelect.addEventListener("change", async function (event){
        getAndCheckRecipient();
    });

    async function getAndCheckRecipient() {
        try {
            let targetPlatform = document.getElementById(receivingRegistryHtmlId).value;
            if (targetPlatform == "") {
                targetPlatform = new URL(location.href).host;
            }
            const recipientId = document.getElementById(recipientIdHtmlId).value;
            const response = await fetch("https://" + targetPlatform + "/api/v1/parties?thumbprint=" + recipientId);
            if (response.status == "200") {
                const contact = JSON.parse(await response.text())[0];
                document.getElementById(transfereePublicKeyTextArea).value = contact.publicKey;
                document.getElementById(fetchRecipientCheckmark).innerHTML  = "✅";
            } else {
                document.getElementById(fetchRecipientCheckmark).innerHTML = "❌";
            }
        } catch(error) {
            document.getElementById(fetchRecipientCheckmark).innerHTML = "❌";
        }
    }
}
