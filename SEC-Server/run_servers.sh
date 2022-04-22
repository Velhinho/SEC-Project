echo How many replicas?:
read replicaNumber
echo reset database? "n" for no, "y" for yes:
read reset
if [ $reset == "y" ]; then
    for (( i=1; i<=$replicaNumber; i++ ))
    do
        konsole -e mvn compile exec:java -DreplicaNumber=$i -Dreset=1 &
    done
else
    for (( i=1; i<=$replicaNumber; i++ ))
    do
        konsole -e mvn compile exec:java -DreplicaNumber=$i &
    done
fi

#gnome-terminal -- command
#or
#xterm -e command
#or
#konsole -e command
#Pretty much:
#terminal -e command