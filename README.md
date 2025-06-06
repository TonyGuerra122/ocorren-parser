# OcorrenParser

**OcorrenParser** é uma biblioteca Java para leitura e interpretação de arquivos de texto com layout fixo, baseando-se em um JSON de definição de campos por tipo de registro.

Projetado para uso com integrações EDI ou sistemas legados que utilizam registros com posições fixas, o OcorrenParser facilita a extração segura e validada dos dados.

---

## 📦 Instalação

### Com Maven

- Adicione o reposiótio do JitPack ao `pom.xml`:
```xml
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
```

- Adicione a dependência no seu `pom.xml`:

```xml
  <dependency>
    <groupId>com.github.TonyGuerra122</groupId>
    <artifactId>ocorren-parser</artifactId>
    <version>1.2.0</version>
  </dependency>
```

---

## 📦 Instalação
-   Arquivos `.txt` com registros de tamanho fixo.

---

## 📄 Exemplo de uso
### Carregar o layout e parsear o arquivo:
```java
import com.tonyguerra.ocorrenparser.core.RecordParser;
import com.tonyguerra.ocorrenparser.enums.LayoutType;

public final class Main {
    public static void main(String[] args) {
        RecordParser parser = RecordParser.fromLayoutType(LayoutType.LFG_OCORREN);
        String json = parser.parserFileToJSON("ocorren.txt");
        
        System.out.println(json);
    }
}
```

---

## ✅ Formato de saída
```json
{
  "000": [
    {
      "IDENTIFICACAO DE REMETENTE": "TONY GUERRA",
      "IDENTIFICAÇÃO DE DESTINATARIO": "JOAO DESTINATARIO",
      "DATA": "1505",
      "HORA": "2409",
      "IDENTIFICAÇÃO DE INTERCAMBIO": "301234567890",
      "FILLER": "1  FILLER DATA"
    }
  ],
  "340": [
    {
      "IDENTIFICACAO DE DOCUMENTO": "DOC15052409301",
      "FILLER": "2                    FILLER DATA AQUI"
    }
  ],
  "341": [
    {
      "CNPJ": "12345678000195",
      "RAZAO SOCIAL": "RAZAO SOCIAL LTDA",
      "FILLER": "FILLER COMPLEMENTAR"
    }
  ],
  "342": [
    {
      "CNPJ EMISSOR NF": "98765432000197",
      "SERIE NF": "ABC",
      "NUMERO NF": "00001234",
      "CODIGO OCORRENCIA": "56",
      "DATA OCORRENCIA": "01150524",
      "HORA OCORRENCIA": "0930",
      "CODIGO OBSERVACAO OCORRENCIA NA ENTRADA": "0",
      "TEXTO LIVRE": "1Texto de ocorrência aqui que pode ser bem grande para teste e deve se",
      "FILLER": "r cortado no limite de 70 caracteresFILLER FINAL"
    }
  ]
}
```

---

## 📑 Layout Permitido
O **Arquivo Ocorren** deverá vir com os campos mapeado conforme o [lfg_ocorren.json](src/main/resources/layouts/lfg_ocorren.json)

---

## 🛠 Funcionalidades
-   ✔️ Suporte a registros múltiplos (000, 340, 341, etc)

-   ✔️ Validação de campos obrigatórios

-   ✔️ Validação alfanumérica e numérica

-   ✔️ Agrupamento automático por recordType

-   ✔️ Exportação JSON formatado

---

## 🧪 Requisitos
-   ☕ **Java 21+**
-   **[Jackson Databind](https://github.com/FasterXML/jackson)**

## 👨‍💻 Autor
Desenvolvido por **[Tony Guerra](https://anthonyguerra.com.br)**