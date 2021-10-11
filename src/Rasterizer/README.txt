Rasterizer written for my graphics course.  The beginning of the file is mostly functions and data structures provided by my professor, but the transform algorithms (lines 89 - 177) were written by me, as was everything beyond line 435.  This code is designed to read in data in the form of VTK-defined triangles consisting of three vertices with x, y, and z coordinates, as well as three-dimensional color and normal vectors for each vertex.  This code encompasses basic rasterization, as well as color interpolation, depth interpolation and triangle overlap, Phong shading, and arbitrary camera position.

For compilation, I have included the CMakeLists.txt file that I used (this project was developed on Mac OS X), as well as the VTK geometry that provided the most interesting output.  The version of CMake used was 3.0.2 and the version of VTK was 6.1.0.

In order to compile and run, use the following commands in order:

cmake .
make

This will create a project entitled rasterizer.app which contains the executable.  Run
./rasterizer.app/Contents/MacOS/rasterizer 
(if you are using OS X)

and by default, four frames should be rendered as c1.png, c2.png, c3.png, and c4.png.

Language: C++
Last modified: 11/5/2014