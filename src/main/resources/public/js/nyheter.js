
fetch("mock-nyheter.json")
    .then(Response => {
        if(!Response.ok) throw new Error("Feil: " + Response.status);
        return Response.json();
    })
    .then(nyheter => {
         
        const container = document.querySelector("#nyheter");
        
        nyheter.forEach(nyhet => {
            const kort = document.createElement("article");

            kort.innerHTML = `
                 <h3>${nyhet.tittel}</h3>
                 <p>${nyhet.innhold}</p>
                 <p>${nyhet.publisert_dato}</p>
                 <a href="#">Les mer</a>
            `;

            container.appendChild(kort);

        });
    })
    .catch(error => {
        console.error("kunne ikke hente nyheter");
    })
