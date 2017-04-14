# Location-Task
A simple Location App which tracks a users journey along with an associated routed polyline and a journey time.

Once the app is opened, the user will be prompted to turn the devices location on (if it's not on already). 

The GoogleApiClient will then connect in the onResume method, the user is then able to start his/her shift by clicking the "Start Shift"
button and is also able to end his/her shift by clicking the "End Shift" button. Two markers will be added at the 
start and end points of the shift and a polyline will be drawn between these two points. A text view containing the 
shift duration will appear.
 
