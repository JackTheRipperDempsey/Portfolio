cmake_minimum_required(VERSION 2.8.12.1)

PROJECT(hdTest)
SET(VTK_DIR /Users/jackdempsey/Downloads/VTK-6.1.0)

find_package(VTK REQUIRED)
include(${VTK_USE_FILE})

add_executable(volumeRenderer MACOSX_BUNDLE volumeRenderer)

if(VTK_LIBRARIES)
  target_link_libraries(volumeRenderer ${VTK_LIBRARIES})
else()
  target_link_libraries(volumeRenderer vtkHybrid)
endif()
