
# RevisorChecker

The purpose of this program is to automate as much of the auditors work as possible

#### Commands
    
    t - two files/bookkeeping years

followed by

    bank.pdf analisys.pdf (analisys2.pdf) ledger.pdf (ledger2.pdf) [number of members] [number of members last year]

#### Examples of arguments to run
A check that is only for one bookkeeping year:

    path/to/banking.pdf path/to/analysis.pdf path/to/ledger.pdf 6 7

If a check is over two bookkeeping years (SP1 or ansvarsbefrielse):

    t path/to/banking.pdf path/to/analysis1.pdf path/to/analysis2.pdf path/to/ledger1.pdf path/to/ledger2.pdf 7 6
