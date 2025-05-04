package etf.ri.rma.newsfeedapp.data

import etf.ri.rma.newsfeedapp.model.NewsItem


object NewsData {
    fun getAllNews(): List<NewsItem> {
        return listOf(

            NewsItem("1",  "Profesor Jahić predstavio važnu knjigu o Bošnjacima u Drugom svjetskom ratu", "U organizaciji Odsjeka za historiju Filozofskog fakulteta u Tuzli i Vijeća kongresa bošnjačkih intelektualaca – Regionalni odbor Tuzla, večeras je u amfiteatru Filozofskog fakulteta Tuzla promovisana knjiga profesora Adnana Jahića ‘Između čekića i...’", null, "Nauka / tehnologija", true , "vijesti.ba",  "19-11-2024"),
            NewsItem("2", "Objavljena kompletna presuda protiv Dodika: Pojašnjeno zašto su brojne primjedbe odbrana bile neutemeljene", "Sud BiH je objavio kompletnu prvostepenu presudu predsjedniku RS Miloradu Dodiku i v.d. direktora 'Službenog glasnika RS' Milošu Lukiću. Riječ je o dokumentu od 150…", null, "Politika",  false,  "Dnevni avaz", "27-03-2025" ),
            NewsItem("4", "Pobjeda Zmajeva u Bukureštu", "U prvom kolu grupe H kvalifikacija za Svjetsko prvenstvo 2026, reprezentacija Bosne i Hercegovine savladala je Rumuniju rezultatom 1:0 u utakmici koja je odigrana na Nacionalnoj areni u Bukureštu.", null, "Sport", false, "Fudbalski savez BiH", "21-03-2025"),
            NewsItem("5", "Košarkaši BiH plasirali se na Eurobasket 2025!", "U pretposljednjem kolu kvalifikacija za Eurobasket 2025, košarkaši Bosne i Hercegovine pobijedili su Kipar rezultatom 108:62. Ovom pobjedom, uz poraz Hrvatske od Francuske, osigurali su plasman na Evropsko prvenstvo.", null, "Sport", true, "Tuzlanski.ba", "21-02-2025"),
            NewsItem("6", "BH učenici briljirali na iFest2 takmičenju nauke, tehnologije i inovacija",  "Bosanskohercegovački učenici ostvarili su značajan uspjeh na iFest2 takmičenju iz oblasti nauke, tehnologije i inovacija.", null, category = "Nauka / tehnologija",  true, "vijesti.ba",  "29-03-2025"),

            NewsItem("3","Vukanović prozvao Čovića - smatra ga licemjerom...","Pozvao ga da se javno izjasni o ponašanju Milorada Dodika, kazavši da je nemoguće zalagati se za evropske integracije, a podržavati ovakve poteze..." ,null, "Politika", false, "oslobodjenje.ba", "30-03-2025"),

            NewsItem("7", "Na Univerzitetu u Sarajevu \"Dani otvorenih vrata\" 4. i 5. aprila", "Univerzitet u Sarajevu najavljuje \"Dane otvorenih vrata\", koji će biti održani 4. i 5. aprila na fakultetima i akademijama Univerziteta.", null, "Nauka / tehnologija", false, "VijestiBa", "03-04-2025" ),
            NewsItem("8", "AI budućnost u BiH: ETF Sarajevo pokreće inovativni studijski program uz podršku dijaspore", "Elektrotehnički fakultet Univerziteta u Sarajevu pokreće novi studijski program fokusiran na umjetnu inteligenciju, uz značajnu podršku bosanskohercegovačke dijaspore.", null, "Nauka / tehnologija", true, "Klix.ba", "04-04-2025"),
            NewsItem("9", "Ramo Isak o hapšenju Dodika: 'Policajci koji se boje pucanja neka idu raditi u obdanište'", "Ministar unutrašnjih poslova Federacije Bosne i Hercegovine Ramo Isak gostovao je u emisiji Istraga sedmice na Hayat televiziji. Isak je govorio o hapšenju predsjednika bosanskohercegovačkog entiteta RS Milorada Dodika.", null, "Politika", false, "Radiosarajevo.ba", "07-04-2025"),
            NewsItem("10", "Ramo Isak o vožnji helikopterom: 'Jesam se snimao i hoću, trebam li se pravdati?'", "Federalni ministar unutrašnjih poslova Ramo Isak osvrnuo se na kritike zbog vožnje službenim helikopterom, kao i dijeljenju videa na društvenim mrežama.", null, "Politika", true, "Klix", "21-09-2023"),
            NewsItem("11", "Ostaje pitanje kakva bi karijera Nurkića bila bez povreda", "Bh. košarkaš Jusuf Nurkić, član Charlotte Hornetsa, uskoro bi se mogao vratiti na teren nakon ponovnog odsustva zbog povrede.", null, "Sport", false, "Sport.ba", "10-03-2025"
            ),
            NewsItem("12", "Musa nakon utakmice sa Parisom: \"Kada se borimo ovako, niko...\"", "Nakon pobjede Real Madrida nad ekipom Parisa u Euroligi, bh. košarkaš Džanan Musa dao je kratku izjavu za medije. Musa je meč završio sa 12 poena, četiri skoka i...", null, "Sport", false, "SportSport.ba", "19-12-2024"),
            NewsItem("13", "Anel Ahmedhodžić napustio reprezentaciju BiH: ‘Nemam više...’", "Ahmedhodžić je odlučio da javno odgovori na sve prozivke i otkrije detalje o svojoj odluci da se povuče iz reprezentacije Bosne i Hercegovine. Ahmedhodžić je napustio kamp...", null, "Sport", false, "Radiosarajevo.ba", "11-09-2024"),
            NewsItem("14", "Izetbegović: Siguran sam da će Dodik biti uhapšen, ne smijemo upasti u njegovu zamku", "Predsjednik Stranke demokratske akcije Bakir Izetbegović siguran je da će predsjednik Republike Srpske Milorad Dodik biti uhapšen, no nije siguran da će to biti brzo i lako.", null, "Politika", false, "Fena", "07-04-2025"
            ),
            NewsItem("15", "Nauka se vraća u BiH: Održana Evropska noć istraživača u Sarajevu",
                "Koordinatorica Evropske noći istraživača u BiH Lejla Čamo ističe da BiH danas, zajedno sa još 400 gradova u 25 europskih zemalja, slavi nauku. Različiti su sadržaji za razne generacije. 'Nauka se vraća u Bosnu i Hercegovinu nakon dvogodišnje pauze, družimo se u 11 gradova BiH.'",
                null, "Nauka / tehnologija",
                true, "radiosarajevo.ba",
                "27-09-2024"
            ),
            NewsItem("16", "Knjige o Aliji Izetbegoviću uvrštene u Nacionalnu i sveučilišnu knjižnicu Hrvatske",
                "Četiri knjige direktora Arhiva Federacije BiH Adamira Jerkovića, trilogija 'Alija izbliza' (I-III) i 'Sjećanja na Aliju Izetbegovića', uvrštene su u fond Nacionalne i sveučilišne knjižnice Hrvatske.",
                null,
                "Nauka / tehnologija",
                false, "Cazin.NET",
                "17-06-2015"
            ),
            NewsItem("17", "NOVI BODOVI ZA NAŠU NAJBOLJU SKIJAŠICU: Muzaferija ostvarila drugi najbolji rezultat u veleslalomu na prvenstvu Belgije", "Najbolja bosanskohercegovačka alpska skijašica Elvedina Muzaferija osvojila je osmo mjesto na Državnom prvenstvu Belgije, u disciplini veleslalom za žene, koje je danas održano u francuskom Val d’Isereu. Današnji rezultat donio joj je novih 36.44 FIS poena...", null, "Sport", false, "Klix.ba", "07-04-2025"
            ),
            NewsItem(
                "18",
                "FK Igman Konjic u finiš sezone sa novim trenerom: Jedno ime kao glavni kandidat",
                "FK Igman Konjic do kraja sezone u Wwin ligi očekuje grčevita borba za opstanak, a obzirom da je ostalo deset kola, svaka naredna utakmica je veoma važna za Igman.",
                null,
                "Sport",
                false,
                "Klix.ba",
                "01-04-2025"
            ),
            NewsItem("19", "Velež i Sarajevo igraju derbi, meč iz Mostara u direktnom TV prenosu", "Fudbalske ekipe Veleža i Sarajeva igraju derbi susret koji će biti prenošen uživo na televiziji iz Mostara.", null, "Sport", true, "Radiosarajevo.ba", "08-03-2025"
            ),
            NewsItem("20", "ODLIČNE VIJESTI ZA SELEKTORA SERGEJA BARBAREZA: Zmaj se našao u početnoj postavi svog kluba poslije više od mjesec",
                "Bh. reprezentativac Samed Baždar našao se u početnoj postavi po prvi put nakon više od mjesec dana - posljednji put je startao još 22. februara. Na terenu je proveo 76 minuta, kada ga je trener povukao iz igre - samo dvije minute prije nego što je pao odlučujući pogodak, prenosi Raport.", null, "Sport",
                false,
                "Raport",
                "07-04-2025"
            )

       )


    }
    fun getNewsById(id: String): NewsItem? {
        return getAllNews().find { it.id == id }
    }
    }

