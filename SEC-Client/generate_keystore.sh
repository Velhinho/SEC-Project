echo Insert name for keystore file:
read keyname
echo Insert password for new keystore:
read pw
pathname="src/main/resources/$keyname"
keytool -genkeypair -keyalg RSA -validity 999 -keystore $pathname -alias mykey -keypass $pw -storepass $pw -dname "cn=Grupo 21, ou=SEC, o=Técnico, c=PT"
