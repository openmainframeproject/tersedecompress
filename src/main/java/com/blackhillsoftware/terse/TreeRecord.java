package com.blackhillsoftware.terse;

/*Inner classe and globals used as data structures for spack decode*/
/* Might be possible to wrap the Tree structure in a class along with
 * its initializer method, which would be cleaner, but unnecessarily anal??
 */
class TreeRecord {

    int Left;
    int Right;
    int Back;
    int NextCount;
}
