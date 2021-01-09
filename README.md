
# RevisorChecker (WIP - not final)

The purpose of this program is to automate as much of the auditors work as possible

#### Commands

    r - regular check
    a - "ansvarsbefrielse" check
    
    o - one file
    t - two files

followed by

    bank.pdf analisys.pdf (analisys2.pdf) (ledger.pdf (ledger2.pdf))


#### Examples of arguments to run ("pre-release"):
A regular check SP3:

    r o path/to/banking.pdf path/to/analysis.pdf

If a regular check is over two bookkeeping years (SP1):

    r t path/to/banking.pdf path/to/analysis1.pdf path/to/analysis2.pdf
If check is done before a "ansvarsbefrielse" over one bookkeeping year

    a o path/to/banking.pdf path/to/analysis.pdf path/to/ledger.pdf

If check is done before a "ansvarsbefrielse" over two bookkeeping year

    a t path/to/banking.pdf path/to/analysis2.pdf path/to/analysis2.pdf path/to/ledger1.pdf path/to/ledger2.pdf
