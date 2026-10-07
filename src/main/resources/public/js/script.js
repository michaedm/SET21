fetch("/api/nyheter")
    .then(response => response.json())
    .then(data => {

        let nyheter = document.getElementById("nyheter");

        data.forEach(nyhet => {

            let article = document.createElement("article");

            article.innerHTML = `
                <h3>${nyhet.tittel}</h3>
                <p>${nyhet.innhold}</p>
                <time>${nyhet.publisert_dato}</time>
                <a href="#">Les mer</a>
                <img class="article_img" src="${nyhet.bilde_url}" alt="Nyhetsbilde">
            `;

            nyheter.appendChild(article);
        });
    });