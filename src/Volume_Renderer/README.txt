This code contains the volume-renderer I wrote using 3D texture for a rectilinear grid-based dataset meant to allow the visualization of a supernova.  Some code was copied or modified from examples used by the professor in his graphics course; these have been marked.  This code contains multiple, commented-out applications of various rotations and color transfer functions, as my the video for my project depicted the camera rotating around the supernova while the colors gradually changed.

Language: C++
Last modified: 12/9/2014

For compilation, I have included the CMakeLists.txt file that I used (this project was developed on Mac OS X), as well as the VTK geometry.  The version of CMake used was 3.0.2 and the version of VTK was 6.1.0.

In order to compile and run, use the following commands in order:

cmake .
make

This will create a project entitled volumeRenderer.app which contains the executable.  Run
./volumeRenderer.app/Contents/MacOS/volumeRenderer 
(if you are using OS X)

If looking through the source code, you might notice several large sections of commented-out code.  One of these contains a number of different transfer functions, including some that change depending on the frame.  Another alters the direction of rotation.  If mp4s showing these effects are desired, I would be happy to provide them through Google Drive.

Language: C++
Last modified: 11/5/2014