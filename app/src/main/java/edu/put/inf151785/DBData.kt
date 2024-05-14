package edu.put.inf151785

// Dane łądowane do bazy danych przy jej utworzeniu
object DBData {
    val trails = listOf(
        Trail("Malta trybuny",
            "Szlak turystyczny \"Malta Trybuny\" w Poznaniu oferuje wyjątkowe doznania spacerowe wokół malowniczego Jeziora Maltańskiego. To urocze miejsce, gdzie można zaznać harmonii z naturą, oddychając świeżym powietrzem i delektując się pięknymi widokami wody i okolicznych terenów.",
            listOf("Biała Góra", "Tor saneczkowy", "Skwer", "Park Tysiąclecia"), R.drawable.malta, 6.1.toFloat(), 0
        ),
        Trail("Poznański Stary Rynek",
            "Stary Rynek i jego okolice to jedne z najciekawszych miejsc do zobaczenia w Poznaniu. Piękny renesansowy ratusz, stare kamienice, urokliwe boczne uliczki, liczne muzea, puby, kawiarnie i spacerujący po ulicach ludzie - wszystko to tworzy niepowtarzalną atmosferę tego miejsca. Stary Rynek jest sercem Poznania.\n" +
                    "Na trasie znajduje się kilka muzeów, które za dodatkową opłatą można zwiedzać codziennie z wyjątkiem poniedziałków.",
            listOf("Ratusz", "Stare Domy Kupieckie", "Pałac Górków", "Koziołki Poznańskie", "Fara Poznańska", "Odwach", "Pałac Działyńskich"), R.drawable.stary_rynek, 0.8.toFloat(), 1
        ),
        Trail("Brzeg Warty",
            "Szlak turystyczny \"Brzeg Warty\" to wyjątkowa trasa w sercu Poznania, która oferuje niezapomniane doświadczenia przyrodnicze i kulturowe. Wiodąc wzdłuż malowniczych brzegów rzeki Warty, szlak zapewnia możliwość podziwiania urokliwych krajobrazów oraz odkrywania bogatej historii tego obszaru.",
            listOf("Stare Koryto Warty", "Park Tadeusza Mazowieckiego", "Most Św. Rocha"), R.drawable.brzeg_warty, 3.4.toFloat(), 0
        ),
        Trail("Wartostrada",
            "Szlak turystyczny \"Wartostrada\" to fascynująca trasa przez Poznań, łącząca najważniejsze zabytki i atrakcje miasta. Nazwa \"Wartostrada\" nawiązuje do wartościowych miejsc, które można odkryć w trakcie spaceru tą trasą.",
            listOf("Śródka", "Nadolnik", "Prak Szelągowski", "Chwaliszewo", "Park Nad Wartą", "Św. Rocha"), R.drawable.wartostrada, 13.4.toFloat(), 0
        ),
        Trail("Park Rataje",
            "Szlak turystyczny \"Park Rataje\" to oaza zieleni w sercu Poznania, zapewniająca relaksujący spacer oraz możliwość aktywnego spędzenia czasu na świeżym powietrzu. Ten malowniczy park oferuje wiele atrakcji dla mieszkańców i turystów.",
            listOf("Kapliczka", "Rogatka przejazdowa", "Lidl", "Kościół"), R.drawable.park_rataje, 1.6.toFloat(), 0
        )
    )
}