// def call() {
//     def changedFiles = step(
//         script: "git diff --name-only HEAD~1".
//         returnStdout: true
//     ).trim().split("\n")

//     changedFiles = changeFiles.findAll { it?.trim() }

//     def codeChanged = changedFile.any {
//         it.startsWith('src/') || 
//         it == 'Dockerfile' ||
//         it == 'pom.xml'
//     } 
//     def helmChanged = changedFiles.any {
//         it.startsWith('helm/')
//     }
    
//     return [
//         codeChanged: codeChanged,
//         helmChanged: helmChanged
//     ]
// }

