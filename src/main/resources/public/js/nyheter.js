
fetch("mock-nyheter.json")
    .then(Response => Response.json())
    .then(nyheter => {
         
        const container = document.querySelector("#nyheter");
        
        nyheter.forEach(nyhet => {
            const kort = document.createElement("article");

            kort.innerHTML = `
                 <h3>${nyhet.tittel}</h3>
                 <p>${nyhet.innhold}</p>
                 <small>${nyhet.publisert_dato}</small>
            `;

            container.appendChild(kort);

        });
    })
    .catch(error => {
        console.error("kunne ikke hente nyheter");
    })
