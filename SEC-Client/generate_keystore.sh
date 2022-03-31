echo Insert name for keystore file:
read keyname
echo Insert password for new keystore:
read pw
resourcepath="src/main/resources/"
pathname="$resourcepath$keyname"
keytool -genkeypair -keyalg RSA -validity 999 -keystore $pathname -alias mykey -keypass $pw -storepass $pw -dname "cn=Grupo 21, ou=SEC, o=Técnico, c=PT"
s=$(keytool -list -rfc -keystore $pathname -alias mykey -storepass $pw | openssl x509 -inform pem -pubkey -noout | sed 's/.*KEY-----//' | tr -d "\n\r")
echo "$s" > ${pathname}.pub