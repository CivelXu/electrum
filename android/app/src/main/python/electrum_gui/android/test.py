from code import InteractiveConsole
import json
import os
from os.path import exists, join
import pkgutil
import unittest
from electrum.logging import get_logger, configure_logging
from electrum import util
from electrum import constants
from electrum import SimpleConfig
from electrum.wallet import Wallet
from electrum.storage import WalletStorage, get_derivation_used_for_hw_device_encryption
from electrum.util import print_msg, print_stderr, json_encode, json_decode, UserCancelled
from electrum.util import InvalidPassword
from electrum.commands import get_parser, known_commands, Commands, config_variables
from electrum import daemon
from electrum import keystore

_logger = get_logger(__name__)


from console import AndroidCommands

util.setup_thread_excepthook()

config_options = {}
config_options['cmdname'] = 'daemon'
config_options['testnet'] = True
config_options['cwd'] = os.getcwd()
config_options['auto_connect'] = True

# is_bundle = getattr(sys, 'frozen', False)
# # fixme: this can probably be achieved with a runtime hook (pyinstaller)
# if is_bundle and os.path.exists(os.path.join(sys._MEIPASS, 'is_portable')):
#     config_options['portable'] = True
#
# # if config_options.get('portable'):
#     config_options['electrum_path'] = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'electrum_data')
#
# if not config_options.get('verbosity'):
#     warnings.simplefilter('ignore', DeprecationWarning)
# if not config_options.get('verbosity'):
#     warnings.simplefilter('ignore', DeprecationWarning)
#
# check uri
uri = config_options.get('url')
if uri:
    if not uri.startswith('bitcoin:'):
        print_stderr('unknown command:', uri)
        sys.exit(1)
    config_options['url'] = uri

# todo: defer this to gui
config = SimpleConfig(config_options)
cmdname = config.get('cmd')
print("cmdname = %s++++" %cmdname)

constants.set_testnet()

testcommond = AndroidCommands(config)
testcommond.start()

name = 'hahahahhahh222'
password = '111111'

#create_wallet

# m = 2
# n = 2
# xpub1 ="Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"
# xpub2 ="Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"
# #testcommond.delete_wallet(name)
# testcommond.set_multi_wallet_info(name,m,n)
# testcommond.add_xpub(xpub1)
# testcommond.add_xpub(xpub2)
# testcommond.create_multi_wallet(name)

testcommond.get_xpub_from_hw()

#load_wallet
testcommond.load_wallet(name, password)
testcommond.select_wallet(name)
info = testcommond.get_wallets_list_info()

#create_tx
all_output = []
output_info = ['tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe', '0.005']
all_output.append(output_info)
output_str = json.dumps(all_output)
fee = 0.001
message = 'test'
ret_str = testcommond.mktx(output_str, message, fee)
ret_list = json.loads(ret_str)
print("tx================%s" % ret_list['tx'])

testcommond.deserialize("0200000000010120f4dd69233e0659b0fb786ad9f1b73c78da5feb89262cf633cc9c2352ad5c770100000000feffffff02801a0600000000002200209a2a629902aa526ba0313caf4eb43e8439a937d2b07e0a0af1735dad6c5d68fd20a10700000000002200209f2f1060a8d7ddf0019b4fae87d022ff257e74b9db40d592a29ca0b1d0e7d07afeffffffff40420f00000000000000040001ff01ffad524c53ff0257548301fb523f1d80000000d6675fe4c36997060b7ba4d10a0c21ddf6494802749299fcfaa9e6d94097993102b18d25f7351a107a73ed1101d4075451c8e19f2e7ede51ea50610a4034263b01010001004c53ff0257548301a525cc62800000001ae565474c80f78bb1a9213a17061c36dcfed41a40fb21b80104c9d1d8963e8703bd8432b0b0a755f6ef45696559b8999bd26e89835495f8ae96c676700052a4750100010052ae47931800")

#get_tx_by_raw
tx_info_str = testcommond.get_tx_info_from_raw(ret_list['tx'])
tx_info = json.loads(tx_info_str)
print("tx info = %s=========" % tx_info)

#parse_qr
qr_data = testcommond.set_qr_data_from_raw_tx(ret_list['tx'])
print("qr_data on ui = %s........" % qr_data)
tx_data = testcommond.parse_qr(qr_data)
print("tx_data = %s---------" % json.loads(tx_data))

#get_history_tx

#get_tx_info

#sign_tx
#testcommond.sign_tx(ret_list['tx'])