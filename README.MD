# Relations

Source code for the Relations Application, based on Eclipse e4 RCP.

## Steps to build

- `git clone https://github.com/aktion-hip/relations.rcp.git`
- cd `relations.rcp`
- `mvn clean verify`

This will create the product (for Windows, Max and Linux) in `/relations.rcp/products/relations.rcp.product/target/products`.

---

Project page: [https://sourceforge.net/projects/relations-rcp/](https://sourceforge.net/projects/relations-rcp/)

Project documentation: [https://relations-rcp.sourceforge.io/](https://relations-rcp.sourceforge.io/)

---

To create a new *Release* (i.e. with adjusted version number), adjust the following files:   

- pom.xml
- products/relations.rcp.product/relations.rcp.product: *version* and *branding*
- features/relations.rcp.feature/feature.xml
- features/relations.add.feature/feature.xml
- bundles/org.elbe.relations/META-INF/MANIFEST.MF