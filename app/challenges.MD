# Problémy

## Načtení SVG sprite
Hned zezačátku jsem narazil na problém načtení `.svg` sprite, resp. jak správně s ním pracovat.
Z tohoto důvodu jsem nechal všechny soubory rozdělené -- bude se s tím jednodušeji pracovat, avšak
implementace nebude tak pěkná, jako se spritem -- budeme muset použít velký `switch`.

## Fragmenty...
Bylo poměrně náročné správně pochopit funkce fragmentů, resp. jejich chování, jak ovlivňují UI.

## Generování deterministického boardu
Bohužel, ne každý board je deterministicky řešitelný. Jednou variantou je implementovat svůj vlastní minesweeper
solver a generovat tolik boardů, dokud nebude jeden řešitelný. Druhou možností je si tyto boardy nechat vygenerovat
od jiné aplikace a uložit si je do souboru. Vybral jsem druhý způsob, protože implementace vlastního solveru je poměrně
obtížné a zdlouhavé a neexistuje na to moc dokumantace/odkazů