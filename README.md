This java program scrapes the content of the German edition of the International Statistical Classification of Diseases and Related Health Problems (ICD) version 10 from http://www.icd-code.de/icd/code/ICD-10-GM.html and saves it in an XML file.

Run the program by downloading
- icd10.jar and
- lib/jsoup-1.8.3.jar
(and maintaining that relative folder structure)

...and executing from the shell
`java -jar icd10.jar`

Possible arguments which you can append to this command are
- `-o [output folder]`  to choose an output folder. By default the program will output to `./icd10` and create the folder if necessary.
- `-d`  to run only a demo version of the program. That is, each XML node gets at most 3 children.
- `-q`  for quiet mode

The program will then download the required webpages from the website and save them to `./[output folder]/html`
When it finished the tree will be printed in the shell:

```
[ICD-10-GM]
  [A00-B99]
    [titel]
      [Kapitel I]
      [Bestimmte infektiöse und parasitäre Krankheiten]
    [inklusive]
      [Krankheiten, die allgemein als ansteckend oder übertragbar anerkannt sind]
    [exklusive]
      [Keimträger oder -ausscheider, einschließlich Verdachtsfällen (Z22.-) Bestimmte lokalisierte Infektionen - siehe im entsprechenden Kapitel des jeweiligen Körpersystems Infektiöse und parasitäre Krankheiten, die Schwangerschaft, Geburt und Wochenbett komplizieren [ausgenommen Tetanus in diesem Zeitabschnitt] (O98.-) Infektiöse und parasitäre Krankheiten, die spezifisch für die Perinatalperiode sind [ausgenommen Tetanus neonatorum, Keuchhusten, Syphilis connata, perinatale Gonokokkeninfektion und perinatale HIV-Krankheit] (P35-P39) Grippe und sonstige akute Infektionen der Atemwege (J00-J22)]
    [A00-A09]
      [titel]
        [Infektiöse Darmkrankheiten]
      [A00.-]
        [titel]
          [Cholera]
        [A00.0]
          [titel]
            [Cholera durch Vibrio cholerae O:1, Biovar cholerae]
          [inklusive]
            [Klassische Cholera]
        [A00.1]
          [titel]
            [Cholera durch Vibrio cholerae O:1, Biovar eltor]
          [inklusive]
            [El-Tor-Cholera]
        [A00.9]
          [titel]
            [Cholera, nicht näher bezeichnet]
      [A01.-]
        [titel]
          [Typhus abdominalis und Paratyphus]
(...)
```

... and the XML document will be written to `./[output folder]/icd10.xml`:

```
<node>
  <url>ICD-10-GM.html</url>
  <content>ICD-10-GM</content>
  <node>
    <url>A00-B99.html</url>
    <content>A00-B99</content>
    <node>
      <content>titel</content>
      <node>
        <content>Kapitel I</content>
      </node>
      <node>
        <content>Bestimmte infektiöse und parasitäre Krankheiten</content>
      </node>
    </node>
    <node>
      <content>inklusive</content>
      <node>
        <content>Krankheiten, die allgemein als ansteckend oder übertragbar anerkannt sind</content>
      </node>
    </node>
    <node>
      <content>exklusive</content>
      <node>
        <content>Keimträger oder -ausscheider, einschließlich Verdachtsfällen (Z22.-) Bestimmte lokalisierte Infektionen - siehe im entsprechenden Kapitel des jeweiligen Körpersystems Infektiöse und parasitäre Krankheiten, die Schwangerschaft, Geburt und Wochenbett komplizieren [ausgenommen Tetanus in diesem Zeitabschnitt] (O98.-) Infektiöse und parasitäre Krankheiten, die spezifisch für die Perinatalperiode sind [ausgenommen Tetanus neonatorum, Keuchhusten, Syphilis connata, perinatale Gonokokkeninfektion und perinatale HIV-Krankheit] (P35-P39) Grippe und sonstige akute Infektionen der Atemwege (J00-J22)</content>
      </node>
    </node>
    <node>
      <url>A00-A09.html</url>
      <content>A00-A09</content>
      <node>
        <content>titel</content>
        <node>
          <content>Infektiöse Darmkrankheiten</content>
        </node>
      </node>
      <node>
        <url>A00.-.html</url>
        <content>A00.-</content>
        <node>
          <content>titel</content>
          <node>
            <content>Cholera</content>
          </node>
        </node>
        <node>
          <content>A00.0</content>
          <node>
            <content>titel</content>
            <node>
              <content>Cholera durch Vibrio cholerae O:1, Biovar cholerae</content>
            </node>
          </node>
          <node>
            <content>inklusive</content>
            <node>
              <content>Klassische Cholera</content>
            </node>
          </node>
        </node>
        <node>
          <content>A00.1</content>
          <node>
            <content>titel</content>
            <node>
              <content>Cholera durch Vibrio cholerae O:1, Biovar eltor</content>
            </node>
          </node>
          <node>
            <content>inklusive</content>
            <node>
              <content>El-Tor-Cholera</content>
            </node>
          </node>
        </node>
        <node>
          <content>A00.9</content>
          <node>
            <content>titel</content>
            <node>
              <content>Cholera, nicht näher bezeichnet</content>
            </node>
          </node>
        </node>
      </node>
      <node>
        <url>A01.-.html</url>
        <content>A01.-</content>
        <node>
          <content>titel</content>
          <node>
            <content>Typhus abdominalis und Paratyphus</content>
          </node>
(...)
```
