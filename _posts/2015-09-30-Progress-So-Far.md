---
layout: post
title: Progress So Far
---
We currently have quite a bit of the core of GRIP complete. However, there are stil several
major hurdles around the UI and OpenCV functions that need to be passed before a release is ready.

We are dynamically generating the bindings for OpenCV using a library called
[javaparser](https://github.com/javaparser/javaparser).
This library allows the generator to navigate the java syntax tree and, using a
predefined list of functions, determine dynamically what code to generate
as well as what default values to use in the method calls.

Since Enumerations in OpenCV are declared as list of integer values as seen below:

{% highlight java linenos %}
/** Various border types, image boundaries are denoted with `|`
 *  @see borderInterpolate, copyMakeBorder */
/** enum cv::BorderTypes */
public static final int
    /** `iiiiii|abcdefgh|iiiiiii`  with some specified `i` */
    BORDER_CONSTANT    = 0,
    /** `aaaaaa|abcdefgh|hhhhhhh` */
    BORDER_REPLICATE   = 1,
    /** `fedcba|abcdefgh|hgfedcb` */
    BORDER_REFLECT     = 2,
    /** `cdefgh|abcdefgh|abcdefg` */
    BORDER_WRAP        = 3,
    /** `gfedcb|abcdefgh|gfedcba` */
    BORDER_REFLECT_101 = 4,
    /** `uvwxyz|absdefgh|ijklmno` */
    BORDER_TRANSPARENT = 5,

    /** same as BORDER_REFLECT_101 */
    BORDER_REFLECT101  =  BORDER_REFLECT_101,
    /** same as BORDER_REFLECT_101 */
    BORDER_DEFAULT     =  BORDER_REFLECT_101,
    /** do not look outside of ROI */
    BORDER_ISOLATED    = 16;
{% endhighlight %}
an enumeration generator is also needed. Thankfully all enumerations are declared
with a relatively similar tag <code>/** enum cv::[Some Type] */</code>.
This allows us to generate enumerations from any of the OpenCV source files.
As these enumerations are generated they are also cached. This allows the generator
to determine the from the function declaration what enum to take as a paramater.
{% highlight java linenos %}
@Namespace("cv")
public static native void GaussianBlur( @ByVal Mat src,
                                @ByVal Mat dst, @ByVal Size ksize,
                                double sigmaX, double sigmaY/*=0*/,
                                int borderType/*=cv::BORDER_DEFAULT*/ );
{% endhighlight %}


The code generation step is a part of the pre-compile build step in Gradle.
This allows us to easily change some of the rules in the generator instead of
making changes to what will eventually become 100+ files representing the different
Open CV functions we want to support.
