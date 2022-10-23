# Scanner for CVE-2022-42889 (Text4Shell)

## Description
this is a scanner for CVE-2022-42889 (Text4Shell) vulnerability

## Usage
### Step
1. Download jar file (jar file is on `preparedJar/text4shell-scanner.jar`)
2. Check java version on your system
3. Run jar file with args (refer to the following example)

### Sample command
```cmd
> java -jar text4shell-scanner.jar /Path/you/want/to/scan
```

### Args
- `--charset`
- `--output`
- `--exclude-prefix`
- `--exclude-pattern`
- `--help`

## Environment
JDK - OpenJDK 11.0.12

## CheckList
- OS
  - [x] Mac
  - [x] Linux
  - [ ] Windows(Not supported)

## License
MIT License