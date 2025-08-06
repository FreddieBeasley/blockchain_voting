document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('voteForm');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();


        const voterId = document.getElementById('voterId').value.trim(); // Public Key
        const voteValue = document.querySelector('input[name="voteValue"]:checked').value; // Vote Value
        const privateKeyString = document.getElementById('sign').value.trim(); // Private Key

        if (!(voterId && voteValue && privateKeyString)) {
            document.getElementById("response").innerText = "Please fill in all fields.";
            return;
        }

        const content = `${voterId}|||${voteValue}`;

        console.log("Private key string length:", privateKeyString.length);
        console.log("First few characters:", privateKeyString.slice(0, 30));

        const privateKey = await stringToPrivateKey(privateKeyString);
        const signature = await signMessage(privateKey, content);

        if (signature == null) {
            document.getElementById("response").innerText = "Failed to sign vote"
            return;
        }

        const data = {
            voterId: voterId,
            voteValue: voteValue,
            signature: signature,
        }

        try {
            const res = await fetch("/vote", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            });

            document.getElementById("response").innerText = await res.text();
        } catch (err) {
            console.log(err);
            document.getElementById("response").innerText = "Failed to submit vote, please try again later"
        }


    })
})

async function stringToPrivateKey(privateKeyString) {
    try {
        const binary = atob(privateKeyString);
        const keyBytes = new Uint8Array([...binary].map(c => c.charCodeAt(0)));

        return await crypto.subtle.importKey(
            "pkcs8",
            keyBytes.buffer,
            {
                name: "RSASSA-PKCS1-v1_5",
                nameCure: "SHA-256"
            },
            false,
            ["sign"]
        );
    }catch(err){
        console.error("Invalid RSA private key", err);
        return null;
    }
}

async function signMessage(privateKey, message) {
    try{
        const encoder = new TextEncoder();
        const signature = await crypto.subtle.sign(
            "RSASSA-PKCS1-v1_5",
            privateKey,
            encoder.encode(message)
        );
        return btoa(String.fromCharCode(...new Uint8Array(signature)));
    } catch(err){
        console.error("RSA signing error", err);
        return null;
    }
}